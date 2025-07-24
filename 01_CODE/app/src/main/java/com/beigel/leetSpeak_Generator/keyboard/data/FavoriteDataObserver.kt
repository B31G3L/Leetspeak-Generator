package com.beigel.leetSpeak_Generator.keyboard.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.repository.LeetRepository
import com.google.gson.Gson
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 📊 LEET DATA PROVIDER
 *
 * ContentProvider für systemweiten Datenaustausch zwischen Haupt-App und Tastatur
 * Ermöglicht der Tastatur Zugriff auf:
 * - Aktuelles Favoriten-Leet
 * - Alle Custom Leets
 * - Live Updates bei Änderungen
 */
class LeetDataProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "com.beigel.leetspeak.provider"
        private const val PATH_FAVORITE_LEET = "favorite_leet"
        private const val PATH_ALL_LEETS = "all_leets"
        private const val PATH_LEET_SETTINGS = "leet_settings"

        val FAVORITE_LEET_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_FAVORITE_LEET")
        val ALL_LEETS_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_ALL_LEETS")
        val LEET_SETTINGS_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_LEET_SETTINGS")

        // URI Matcher
        private const val CODE_FAVORITE_LEET = 1
        private const val CODE_ALL_LEETS = 2
        private const val CODE_LEET_SETTINGS = 3

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_FAVORITE_LEET, CODE_FAVORITE_LEET)
            addURI(AUTHORITY, PATH_ALL_LEETS, CODE_ALL_LEETS)
            addURI(AUTHORITY, PATH_LEET_SETTINGS, CODE_LEET_SETTINGS)
        }

        // Column names
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ICON_RES_ID = "icon_res_id"
        const val COLUMN_TRANSLATIONS = "translations"
        const val COLUMN_IS_FAVORITE = "is_favorite"
        const val COLUMN_MODE = "mode"
        const val COLUMN_CUSTOM_INDEX = "custom_index"
    }

    private lateinit var leetRepository: LeetRepository
    private val gson = Gson()
    private val providerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate(): Boolean {
        return try {
            // Get Hilt EntryPoint for dependency injection
            val entryPoint = EntryPointAccessors.fromApplication(
                context!!.applicationContext,
                LeetDataProviderEntryPoint::class.java
            )
            leetRepository = entryPoint.leetRepository()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            CODE_FAVORITE_LEET -> getFavoriteLeetCursor()
            CODE_ALL_LEETS -> getAllLeetsCursor()
            CODE_LEET_SETTINGS -> getLeetSettingsCursor()
            else -> null
        }
    }

    private fun getFavoriteLeetCursor(): Cursor {
        val cursor = MatrixCursor(arrayOf(
            COLUMN_ID, COLUMN_NAME, COLUMN_TRANSLATIONS,
            COLUMN_ICON_RES_ID, COLUMN_MODE, COLUMN_CUSTOM_INDEX
        ))

        try {
            runBlocking {
                val favoriteResult = leetRepository.loadFavoriteLeet().getOrNull()

                when (favoriteResult) {
                    is LeetRepository.FavoriteLeetResult.Simple -> {
                        cursor.addRow(arrayOf(
                            "simple",
                            "Simple Leet",
                            gson.toJson(getSimpleTranslations()),
                            com.beigel.leetSpeak_Generator.R.drawable.ic_simple_mode,
                            "SIMPLE",
                            -1
                        ))
                    }
                    is LeetRepository.FavoriteLeetResult.Extended -> {
                        cursor.addRow(arrayOf(
                            "extended",
                            "Extended Leet",
                            gson.toJson(getExtendedTranslations()),
                            com.beigel.leetSpeak_Generator.R.drawable.ic_extended_mode,
                            "EXTENDED",
                            -1
                        ))
                    }
                    is LeetRepository.FavoriteLeetResult.Custom -> {
                        cursor.addRow(arrayOf(
                            "custom_${favoriteResult.customIndex}",
                            favoriteResult.leet.name,
                            gson.toJson(favoriteResult.leet.translations),
                            favoriteResult.leet.iconResId,
                            "CUSTOM",
                            favoriteResult.customIndex
                        ))
                    }
                    null -> {
                        // Fallback to Simple
                        cursor.addRow(arrayOf(
                            "simple",
                            "Simple Leet",
                            gson.toJson(getSimpleTranslations()),
                            com.beigel.leetSpeak_Generator.R.drawable.ic_simple_mode,
                            "SIMPLE",
                            -1
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            // Return empty cursor on error
        }

        return cursor
    }

    private fun getAllLeetsCursor(): Cursor {
        val cursor = MatrixCursor(arrayOf(
            COLUMN_ID, COLUMN_NAME, COLUMN_TRANSLATIONS,
            COLUMN_ICON_RES_ID, COLUMN_IS_FAVORITE
        ))

        try {
            runBlocking {
                val allLeets = leetRepository.leets.first()
                val favoriteOptions = leetRepository.getFavoriteLeetOptions().first()
                val favoriteNames = favoriteOptions.map { it.name }.toSet()

                // Add built-in leets
                cursor.addRow(arrayOf(
                    "simple",
                    "Simple Leet",
                    gson.toJson(getSimpleTranslations()),
                    com.beigel.leetSpeak_Generator.R.drawable.ic_simple_mode,
                    favoriteNames.contains("Simple Leet")
                ))

                cursor.addRow(arrayOf(
                    "extended",
                    "Extended Leet",
                    gson.toJson(getExtendedTranslations()),
                    com.beigel.leetSpeak_Generator.R.drawable.ic_extended_mode,
                    favoriteNames.contains("Extended Leet")
                ))

                // Add custom leets
                allLeets.forEachIndexed { index, leet ->
                    cursor.addRow(arrayOf(
                        "custom_$index",
                        leet.name,
                        gson.toJson(leet.translations),
                        leet.iconResId,
                        favoriteNames.contains(leet.name)
                    ))
                }
            }
        } catch (e: Exception) {
            // Return empty cursor on error
        }

        return cursor
    }

    private fun getLeetSettingsCursor(): Cursor {
        val cursor = MatrixCursor(arrayOf(
            "setting_key", "setting_value"
        ))

        try {
            // Add keyboard-specific settings
            cursor.addRow(arrayOf("keyboard_enabled", "true"))
            cursor.addRow(arrayOf("live_preview_enabled", "true"))
            cursor.addRow(arrayOf("gesture_support_enabled", "true"))
            cursor.addRow(arrayOf("auto_suggest_enabled", "true"))
        } catch (e: Exception) {
            // Return empty cursor on error
        }

        return cursor
    }

    // Helper functions for built-in translations
    private fun getSimpleTranslations(): Map<String, String> {
        return mapOf(
            "A" to "4", "B" to "8", "E" to "3", "G" to "6", "H" to "#",
            "I" to "1", "L" to "L", "O" to "0", "S" to "5", "T" to "7", "Z" to "2"
        )
    }

    private fun getExtendedTranslations(): Map<String, String> {
        return mapOf(
            "A" to "4", "B" to "8", "C" to "(", "D" to "|)", "E" to "3",
            "F" to "|=", "G" to "6", "H" to "#", "I" to "!", "J" to "_|",
            "K" to "|<", "L" to "1", "M" to "/\\/\\", "N" to "|\\|", "O" to "0",
            "P" to "9", "Q" to "0_", "R" to "2", "S" to "5", "T" to "7",
            "U" to "|_|", "V" to "\\/", "W" to "\\/\\/", "X" to "><", "Y" to "`/", "Z" to "Z"
        )
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}

/**
 * 🔍 FAVORITE DATA OBSERVER
 *
 * Überwacht Änderungen am Favoriten-Leet und benachrichtigt die Tastatur
 * Ermöglicht Live-Updates ohne App-Neustart
 */
@Singleton
class FavoriteDataObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var contentObserver: ContentObserver? = null
    private var callback: ((CustomLeet?) -> Unit)? = null
    private val observerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startObserving(onFavoriteLeetChanged: (CustomLeet?) -> Unit) {
        this.callback = onFavoriteLeetChanged

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                observerScope.launch {
                    val newFavoriteLeet = queryCurrentFavoriteLeet()
                    callback?.invoke(newFavoriteLeet)
                }
            }
        }

        // Register observer
        context.contentResolver.registerContentObserver(
            LeetDataProvider.FAVORITE_LEET_URI,
            true,
            contentObserver!!
        )

        // Initial load
        observerScope.launch {
            val initialLeet = queryCurrentFavoriteLeet()
            callback?.invoke(initialLeet)
        }
    }

    fun stopObserving() {
        contentObserver?.let { observer ->
            context.contentResolver.unregisterContentObserver(observer)
            contentObserver = null
        }
        callback = null
        observerScope.cancel()
    }

    private suspend fun queryCurrentFavoriteLeet(): CustomLeet? = withContext(Dispatchers.IO) {
        try {
            val cursor = context.contentResolver.query(
                LeetDataProvider.FAVORITE_LEET_URI,
                null, null, null, null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val mode = it.getString(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_MODE))

                    return@withContext when (mode) {
                        "CUSTOM" -> {
                            val name = it.getString(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_NAME))
                            val translationsJson = it.getString(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_TRANSLATIONS))
                            val iconResId = it.getInt(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_ICON_RES_ID))

                            val translations = Gson().fromJson<Map<String, String>>(
                                translationsJson,
                                object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                            )

                            CustomLeet(name, iconResId).apply {
                                setTranslations(translations)
                            }
                        }
                        else -> null // Simple/Extended modes don't need CustomLeet object
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
        return@withContext null
    }

    /**
     * 🔄 Synchrone Abfrage des aktuellen Favoriten (für Init)
     */
    fun getCurrentFavoriteLeet(): CustomLeet? {
        return runBlocking { queryCurrentFavoriteLeet() }
    }

    /**
     * 📊 Query all available leets
     */
    suspend fun getAllAvailableLeets(): List<LeetInfo> = withContext(Dispatchers.IO) {
        val leetList = mutableListOf<LeetInfo>()

        try {
            val cursor = context.contentResolver.query(
                LeetDataProvider.ALL_LEETS_URI,
                null, null, null, null
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_ID))
                    val name = it.getString(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_NAME))
                    val translationsJson = it.getString(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_TRANSLATIONS))
                    val iconResId = it.getInt(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_ICON_RES_ID))
                    val isFavorite = it.getInt(it.getColumnIndexOrThrow(LeetDataProvider.COLUMN_IS_FAVORITE)) == 1

                    val translations = Gson().fromJson<Map<String, String>>(
                        translationsJson,
                        object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                    )

                    leetList.add(LeetInfo(
                        id = id,
                        name = name,
                        translations = translations,
                        iconResId = iconResId,
                        isFavorite = isFavorite
                    ))
                }
            }
        } catch (e: Exception) {
            // Return empty list on error
        }

        return@withContext leetList
    }
}

/**
 * 🎯 Hilt Entry Point für ContentProvider
 * Da ContentProvider nicht direkt Hilt-injiziert werden kann
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface LeetDataProviderEntryPoint {
    fun leetRepository(): LeetRepository
}

/**
 * 📋 Data Classes
 */
data class LeetInfo(
    val id: String,
    val name: String,
    val translations: Map<String, String>,
    val iconResId: Int,
    val isFavorite: Boolean
)

/**
 * 🔒 Permissions für ContentProvider
 */
object LeetDataPermissions {
    const val READ_LEET_DATA = "com.beigel.leetspeak.READ_LEET_DATA"
    const val WRITE_LEET_DATA = "com.beigel.leetspeak.WRITE_LEET_DATA"
}

/**
 * 🛠️ Helper Class für Keyboard-spezifische Datenoperationen
 */
class KeyboardDataHelper(private val context: Context) {

    /**
     * Quick method für Tastatur um aktuelles Favoriten-Leet zu erhalten
     */
    suspend fun getFavoriteLeetForKeyboard(): CustomLeet? {
        return try {
            val cursor = context.contentResolver.query(
                LeetDataProvider.FAVORITE_LEET_URI,
                arrayOf(LeetDataProvider.COLUMN_MODE, LeetDataProvider.COLUMN_NAME,
                    LeetDataProvider.COLUMN_TRANSLATIONS, LeetDataProvider.COLUMN_ICON_RES_ID),
                null, null, null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val mode = it.getString(0)
                    if (mode == "CUSTOM") {
                        val name = it.getString(1)
                        val translationsJson = it.getString(2)
                        val iconResId = it.getInt(3)

                        val translations = Gson().fromJson<Map<String, String>>(
                            translationsJson,
                            object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                        )

                        return@use CustomLeet(name, iconResId).apply {
                            setTranslations(translations)
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if keyboard has necessary permissions
     */
    fun hasRequiredPermissions(): Boolean {
        return try {
            context.contentResolver.query(
                LeetDataProvider.FAVORITE_LEET_URI,
                arrayOf(LeetDataProvider.COLUMN_NAME),
                null, null, null
            )?.use { true } ?: false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}