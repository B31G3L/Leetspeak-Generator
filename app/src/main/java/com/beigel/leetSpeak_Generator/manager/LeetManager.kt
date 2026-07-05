package com.beigel.leetSpeak_Generator.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.utils.ErrorHandler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class LeetManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME        = "LeetSpeakProfiles"
        private const val LEETS_KEY         = "leets"
        private const val CURRENT_LEET_KEY  = "current_leet"
        private const val FAVORITE_LEET_KEY = "favorite_leet"
        private const val OPTIONS_ORDER_KEY  = "options_order"

        const val MODE_SIMPLE   = 0
        const val MODE_EXTENDED = 1
        const val MODE_CUSTOM   = 2
        const val MODE_ABOUT    = 3

        const val FAV_NONE     = -1
        const val FAV_SIMPLE   = -100
        const val FAV_EXTENDED = -101

        private const val TAG = "LeetManager"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson  = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _leets            = MutableStateFlow<List<CustomLeet>>(emptyList())
    private val _currentLeetIndex = MutableStateFlow(0)
    private val _favoriteIndex    = MutableStateFlow(FAV_NONE)
    private val _optionsOrder     = MutableStateFlow<List<Int>>(listOf(FAV_SIMPLE, FAV_EXTENDED))
    private val _isLoaded         = CompletableDeferred<Unit>()

    val leets:            StateFlow<List<CustomLeet>> = _leets.asStateFlow()
    val currentLeetIndex: StateFlow<Int>              = _currentLeetIndex.asStateFlow()
    val favoriteIndex:    StateFlow<Int>              = _favoriteIndex.asStateFlow()
    val optionsOrder:     StateFlow<List<Int>>        = _optionsOrder.asStateFlow()

    val currentLeet: StateFlow<CustomLeet?> = combine(leets, currentLeetIndex) { leets, index ->
        leets.getOrNull(index)
    }.stateIn(scope, SharingStarted.Lazily, null)

    val hasLeets: StateFlow<Boolean> = leets.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Lazily, false)

    init {
        loadLeets()
    }

    private fun loadLeets() {
        scope.launch {
            try {
                val leetsJson      = prefs.getString(LEETS_KEY, null)
                val currentIndex   = prefs.getInt(CURRENT_LEET_KEY, 0)
                val favoriteIndex  = prefs.getInt(FAVORITE_LEET_KEY, FAV_NONE)
                val orderString    = prefs.getString(OPTIONS_ORDER_KEY, null)

                val loadedLeets = if (leetsJson != null) {
                    val type = object : TypeToken<List<CustomLeet>>() {}.type
                    gson.fromJson<List<CustomLeet>>(leetsJson, type) ?: emptyList()
                } else emptyList()

                // Reihenfolge laden
                val loadedOrder = if (orderString != null) {
                    orderString.split(",").mapNotNull { it.trim().toIntOrNull() }
                } else {
                    // Standard: Simple, Extended, dann Custom Leets
                    val defaultOrder = mutableListOf(FAV_SIMPLE, FAV_EXTENDED)
                    loadedLeets.indices.forEach { defaultOrder.add(it) }
                    defaultOrder
                }

                withContext(Dispatchers.Main) {
                    _leets.value            = loadedLeets
                    _currentLeetIndex.value = currentIndex.coerceIn(0, maxOf(0, loadedLeets.size - 1))
                    _favoriteIndex.value    = favoriteIndex
                    _optionsOrder.value     = loadedOrder
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fehler beim Laden", e)
                withContext(Dispatchers.Main) {
                    _leets.value            = emptyList()
                    _currentLeetIndex.value = 0
                    _favoriteIndex.value    = FAV_NONE
                    _optionsOrder.value     = listOf(FAV_SIMPLE, FAV_EXTENDED)
                }
            } finally {
                _isLoaded.complete(Unit)
            }
        }
    }

    private suspend fun ensureLoaded() = _isLoaded.await()

    private suspend fun saveLeets() = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString(LEETS_KEY, gson.toJson(_leets.value))
            .putInt(CURRENT_LEET_KEY, _currentLeetIndex.value)
            .putInt(FAVORITE_LEET_KEY, _favoriteIndex.value)
            .apply()
    }

    private fun saveOptionsOrder() {
        prefs.edit()
            .putString(OPTIONS_ORDER_KEY, _optionsOrder.value.joinToString(","))
            .apply()
    }

    suspend fun addLeet(leet: CustomLeet): ErrorHandler.Result<Int> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to add leet") {
            val currentLeets = _leets.value.toMutableList()
            currentLeets.add(leet)
            val newIndex = currentLeets.size - 1
            _leets.value            = currentLeets
            _currentLeetIndex.value = newIndex

            // Neuen Custom Leet zur Reihenfolge hinzufügen
            val newOrder = _optionsOrder.value.toMutableList()
            newOrder.add(newIndex)
            _optionsOrder.value = newOrder

            saveLeets()
            saveOptionsOrder()
            newIndex
        }

    suspend fun insertLeetAt(index: Int, leet: CustomLeet): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to insert leet") {
            val currentLeets = _leets.value.toMutableList()
            val safeIndex    = index.coerceIn(0, currentLeets.size)
            currentLeets.add(safeIndex, leet)

            if (_favoriteIndex.value >= safeIndex) {
                _favoriteIndex.value = _favoriteIndex.value + 1
            }

            // Indizes in der Reihenfolge anpassen
            val newOrder = _optionsOrder.value.map { id ->
                if (id >= safeIndex) id + 1 else id
            }.toMutableList()
            newOrder.add(safeIndex)
            _optionsOrder.value = newOrder

            _leets.value            = currentLeets
            _currentLeetIndex.value = safeIndex
            saveLeets()
            saveOptionsOrder()
            Log.d(TAG, "✅ Leet '${leet.name}' an Index $safeIndex eingefügt (Undo)")
        }

    suspend fun updateLeet(index: Int, leet: CustomLeet): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to update leet") {
            require(index in 0 until _leets.value.size) { "Ungültiger Index: $index" }
            val currentLeets = _leets.value.toMutableList()
            currentLeets[index] = leet
            _leets.value = currentLeets
            saveLeets()
        }

    suspend fun deleteLeet(index: Int): ErrorHandler.Result<LeetDeletionResult> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to delete leet") {
            require(index in 0 until _leets.value.size) { "Ungültiger Index: $index" }
            val currentLeets = _leets.value.toMutableList()
            val deletedLeet  = currentLeets.removeAt(index)
            val wasFavorite  = _favoriteIndex.value == index
            val wasLastLeet  = currentLeets.isEmpty()

            when {
                _favoriteIndex.value == index -> _favoriteIndex.value = FAV_NONE
                _favoriteIndex.value > index  -> _favoriteIndex.value = _favoriteIndex.value - 1
            }

            val newCurrentIndex = when {
                currentLeets.isEmpty()                        -> 0
                _currentLeetIndex.value >= currentLeets.size  -> currentLeets.size - 1
                else                                          -> _currentLeetIndex.value
            }

            // Index aus der Reihenfolge entfernen und verbleibende Indizes anpassen
            val newOrder = _optionsOrder.value
                .filter { it != index }
                .map { id -> if (id > index) id - 1 else id }
            _optionsOrder.value = newOrder

            _leets.value            = currentLeets
            _currentLeetIndex.value = newCurrentIndex
            saveLeets()
            saveOptionsOrder()

            LeetDeletionResult(deletedLeet, wasFavorite, wasLastLeet)
        }

    suspend fun reorderOptions(fromIdentifier: Int, toIdentifier: Int): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to reorder options") {
            val current = _optionsOrder.value.toMutableList()
            val fromIdx = current.indexOf(fromIdentifier)
            val toIdx   = current.indexOf(toIdentifier)
            if (fromIdx >= 0 && toIdx >= 0) {
                current.add(toIdx, current.removeAt(fromIdx))
                _optionsOrder.value = current
                saveOptionsOrder()
            }
        }

    suspend fun setCurrentLeetIndex(index: Int): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to set current leet") {
            require(index in 0 until _leets.value.size) { "Ungültiger Index: $index" }
            _currentLeetIndex.value = index
            saveLeets()
        }

    suspend fun setFavorite(mode: Int, customIndex: Int = 0): ErrorHandler.Result<Unit> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to set favorite") {
            val favoriteIndex = when (mode) {
                MODE_SIMPLE   -> FAV_SIMPLE
                MODE_EXTENDED -> FAV_EXTENDED
                MODE_CUSTOM   -> {
                    require(customIndex in 0 until _leets.value.size) {
                        "Ungültiger Custom-Index: $customIndex"
                    }
                    customIndex
                }
                else -> throw IllegalArgumentException("Ungültiger Modus: $mode")
            }
            _favoriteIndex.value = favoriteIndex
            saveLeets()
        }

    suspend fun toggleFavorite(mode: Int, customIndex: Int = 0): ErrorHandler.Result<Boolean> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to toggle favorite") {
            val targetIndex = when (mode) {
                MODE_SIMPLE   -> FAV_SIMPLE
                MODE_EXTENDED -> FAV_EXTENDED
                MODE_CUSTOM   -> customIndex
                else          -> throw IllegalArgumentException("Ungültiger Modus: $mode")
            }
            val isCurrentlyFavorite = _favoriteIndex.value == targetIndex
            _favoriteIndex.value    = if (isCurrentlyFavorite) FAV_NONE else targetIndex
            saveLeets()
            !isCurrentlyFavorite
        }

    fun isFavorite(mode: Int, customIndex: Int = 0): Boolean {
        val targetIndex = when (mode) {
            MODE_SIMPLE   -> FAV_SIMPLE
            MODE_EXTENDED -> FAV_EXTENDED
            MODE_CUSTOM   -> customIndex
            else          -> return false
        }
        return _favoriteIndex.value == targetIndex
    }

    suspend fun getFavoriteLeetInfo(): FavoriteLeetInfo? {
        ensureLoaded()
        return when (val favIndex = _favoriteIndex.value) {
            FAV_SIMPLE   -> FavoriteLeetInfo(MODE_SIMPLE, -1, null)
            FAV_EXTENDED -> FavoriteLeetInfo(MODE_EXTENDED, -1, null)
            FAV_NONE     -> null
            else         -> {
                val leet = _leets.value.getOrNull(favIndex)
                if (leet != null) FavoriteLeetInfo(MODE_CUSTOM, favIndex, leet) else null
            }
        }
    }

    suspend fun createLeetWithSimpleDefaults(name: String): ErrorHandler.Result<CustomLeet> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to create leet") {
            val leet = CustomLeet.createWithSimpleDefaults(name)
            addLeet(leet).getOrNull()
            leet
        }

    suspend fun createLeetWithExtendedDefaults(name: String): ErrorHandler.Result<CustomLeet> =
        ErrorHandler.safeExecuteSuspend(context, "Failed to create leet") {
            val leet = CustomLeet.createWithExtendedDefaults(name)
            addLeet(leet).getOrNull()
            leet
        }

    fun cleanup() = scope.cancel()

    data class LeetDeletionResult(
        val deletedLeet: CustomLeet,
        val wasFavorite: Boolean,
        val wasLastLeet: Boolean
    )

    data class FavoriteLeetInfo(
        val mode: Int,
        val customIndex: Int,
        val customLeet: CustomLeet?
    )
}