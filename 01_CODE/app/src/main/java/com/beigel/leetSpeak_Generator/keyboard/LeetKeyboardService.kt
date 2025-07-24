package com.beigel.leetSpeak_Generator.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.keyboard.engine.LiveTranslationEngine
import com.beigel.leetSpeak_Generator.keyboard.data.FavoriteDataObserver
import com.beigel.leetSpeak_Generator.keyboard.ui.LeetKeyboardUI
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.*

/**
 * 🎹 LEETSPEAK KEYBOARD - Custom IME Service
 *
 * Systemweite Leetspeak-Tastatur die in allen Android-Apps funktioniert
 * Features:
 * - Live Translation während dem Tippen
 * - Sync mit Favoriten-Leet aus Haupt-App
 * - Gestures & Smart Suggestions
 * - Custom Compose UI
 */
class LeetKeyboardService(override val lifecycle: Lifecycle) : InputMethodService(),
    ViewModelStoreOwner, SavedStateRegistryOwner {

    // Lifecycle Management für Compose
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    // Core Components
    private lateinit var translationEngine: LiveTranslationEngine
    private lateinit var favoriteDataObserver: FavoriteDataObserver
    private lateinit var keyboardViewModel: LeetKeyboardViewModel

    // UI State
    private var composeView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Keyboard State
    private var currentLeetMode = LeetTranslator.TranslationMode.SIMPLE
    private var isLeetModeActive = true
    private var inputBuffer = StringBuilder()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        // Initialize Core Components
        initializeComponents()

        // Start observing favorite leet changes
        startFavoriteObserver()
    }

    private fun initializeComponents() {
        translationEngine = LiveTranslationEngine()
        favoriteDataObserver = FavoriteDataObserver(this)
        keyboardViewModel = LeetKeyboardViewModel(translationEngine, favoriteDataObserver)
    }

    private fun startFavoriteObserver() {
        favoriteDataObserver.startObserving { newLeet ->
            serviceScope.launch {
                translationEngine.updateFavoriteLeet(newLeet)
                currentLeetMode = if (newLeet != null) {
                    LeetTranslator.TranslationMode.CUSTOM
                } else {
                    LeetTranslator.TranslationMode.SIMPLE
                }
                keyboardViewModel.updateCurrentMode(currentLeetMode, newLeet)
            }
        }
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            composeView = this

            // Set up Lifecycle owners for Compose
            setViewTreeViewModelStoreOwner(this@LeetKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@LeetKeyboardService)

            setContent {
                LeetKeyboardUI(
                    viewModel = keyboardViewModel,
                    onKeyPress = ::handleKeyPress,
                    onSpecialAction = ::handleSpecialAction,
                    onModeToggle = ::toggleLeetMode,
                    onSettingsOpen = ::openMainApp
                )
            }
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)

        // Reset input state
        inputBuffer.clear()

        // Update context-based settings
        updateContextualSettings(attribute)

        // Notify UI of input start
        keyboardViewModel.onInputStarted(attribute)
    }

    private fun updateContextualSettings(editorInfo: EditorInfo?) {
        val packageName = editorInfo?.packageName?.toString() ?: ""

        // Smart context detection
        when {
            packageName.contains("whatsapp") -> {
                // WhatsApp: Use simple leet for readability
                keyboardViewModel.setSuggestedMode(LeetTranslator.TranslationMode.SIMPLE)
            }
            packageName.contains("discord") || packageName.contains("game") -> {
                // Gaming apps: Use extended leet
                keyboardViewModel.setSuggestedMode(LeetTranslator.TranslationMode.EXTENDED)
            }
            editorInfo?.inputType?.and(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) != 0 -> {
                // Password fields: Disable leet mode
                isLeetModeActive = false
            }
            else -> {
                // Default: Use favorite leet
                isLeetModeActive = true
            }
        }
    }

    /**
     * 🔤 Hauptfunktion: Tastendrücke verarbeiten
     */
    private fun handleKeyPress(key: String) {
        val inputConnection = currentInputConnection ?: return

        when {
            key.length == 1 && key[0].isLetter() -> {
                handleLetterKey(key[0], inputConnection)
            }
            key == "SPACE" -> {
                handleSpaceKey(inputConnection)
            }
            key == "BACKSPACE" -> {
                handleBackspaceKey(inputConnection)
            }
            key == "ENTER" -> {
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            else -> {
                inputConnection.commitText(key, 1)
            }
        }

        // Update UI with current translation
        keyboardViewModel.updateInputBuffer(inputBuffer.toString())
    }

    private fun handleLetterKey(char: Char, inputConnection: InputConnection) {
        inputBuffer.append(char)

        if (isLeetModeActive) {
            val translatedChar = translationEngine.translateChar(char, currentLeetMode)
            inputConnection.commitText(translatedChar, 1)

            // Update live preview
            keyboardViewModel.updateLivePreview(inputBuffer.toString(), translationEngine.translateWord(inputBuffer.toString()))
        } else {
            inputConnection.commitText(char.toString(), 1)
        }
    }

    private fun handleSpaceKey(inputConnection: InputConnection) {
        if (inputBuffer.isNotEmpty() && isLeetModeActive) {
            // Complete word translation
            val currentWord = inputBuffer.toString()
            val translatedWord = translationEngine.translateWord(currentWord)

            // Show suggestion if different from live translation
            keyboardViewModel.addWordSuggestion(currentWord, translatedWord)
        }

        inputBuffer.clear()
        inputConnection.commitText(" ", 1)
        keyboardViewModel.clearLivePreview()
    }

    private fun handleBackspaceKey(inputConnection: InputConnection) {
        val selectedText = inputConnection.getSelectedText(0)

        if (selectedText != null && selectedText.isNotEmpty()) {
            inputConnection.commitText("", 1)
        } else {
            inputConnection.deleteSurroundingText(1, 0)
        }

        // Update buffer
        if (inputBuffer.isNotEmpty()) {
            inputBuffer.deleteCharAt(inputBuffer.length - 1)
            keyboardViewModel.updateInputBuffer(inputBuffer.toString())
        }
    }

    /**
     * 🎯 Spezielle Aktionen (Gestures, Shortcuts etc.)
     */
    private fun handleSpecialAction(action: String) {
        when (action) {
            "TOGGLE_LEET" -> toggleLeetMode()
            "QUICK_SETTINGS" -> openMainApp()
            "LEET_SIGNATURE" -> insertLeetSignature()
            "CLEAR_SUGGESTIONS" -> keyboardViewModel.clearSuggestions()
            "SWITCH_MODE" -> cycleLeetMode()
        }
    }

    private fun toggleLeetMode() {
        isLeetModeActive = !isLeetModeActive
        keyboardViewModel.setLeetModeActive(isLeetModeActive)

        // Visual feedback
        if (isLeetModeActive) {
            keyboardViewModel.showToast("🎯 Leet Mode ON")
        } else {
            keyboardViewModel.showToast("📝 Normal Mode")
        }
    }

    private fun cycleLeetMode() {
        currentLeetMode = when (currentLeetMode) {
            LeetTranslator.TranslationMode.SIMPLE -> LeetTranslator.TranslationMode.EXTENDED
            LeetTranslator.TranslationMode.EXTENDED -> LeetTranslator.TranslationMode.CUSTOM
            LeetTranslator.TranslationMode.CUSTOM -> LeetTranslator.TranslationMode.SIMPLE
        }

        keyboardViewModel.updateCurrentMode(currentLeetMode, translationEngine.getCurrentCustomLeet())
        keyboardViewModel.showToast("Mode: ${getModeDisplayName()}")
    }

    private fun getModeDisplayName(): String {
        return when (currentLeetMode) {
            LeetTranslator.TranslationMode.SIMPLE -> "Simple Leet"
            LeetTranslator.TranslationMode.EXTENDED -> "Extended Leet"
            LeetTranslator.TranslationMode.CUSTOM -> translationEngine.getCurrentCustomLeet()?.name ?: "Custom Leet"
        }
    }

    private fun insertLeetSignature() {
        val signatures = listOf(
            "|_337 5P34K 6363|2470|2",
            "pwn3d by l33t",
            "1337 h4x0r",
            "н4ск тне ρℓαηєт"
        )

        val signature = signatures.random()
        currentInputConnection?.commitText(" $signature ", 1)
        keyboardViewModel.showToast("🎮 Leet signature inserted!")
    }

    private fun openMainApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        inputBuffer.clear()
        keyboardViewModel.onInputFinished()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        favoriteDataObserver.stopObserving()
        store.clear()
    }
}

/**
 * 🧠 ViewModel für Keyboard State Management
 */
class LeetKeyboardViewModel(
    private val translationEngine: LiveTranslationEngine,
    private val favoriteDataObserver: FavoriteDataObserver
) {

    // UI State
    private val _currentMode = mutableStateOf(LeetTranslator.TranslationMode.SIMPLE)
    val currentMode: State<LeetTranslator.TranslationMode> = _currentMode

    private val _isLeetModeActive = mutableStateOf(true)
    val isLeetModeActive: State<Boolean> = _isLeetModeActive

    private val _livePreview = mutableStateOf("")
    val livePreview: State<String> = _livePreview

    private val _suggestions = mutableStateOf<List<String>>(emptyList())
    val suggestions: State<List<String>> = _suggestions

    private val _toastMessage = mutableStateOf<String?>(null)
    val toastMessage: State<String?> = _toastMessage

    private val _currentCustomLeet = mutableStateOf<CustomLeet?>(null)
    val currentCustomLeet: State<CustomLeet?> = _currentCustomLeet

    fun updateCurrentMode(mode: LeetTranslator.TranslationMode, customLeet: CustomLeet?) {
        _currentMode.value = mode
        _currentCustomLeet.value = customLeet
    }

    fun setLeetModeActive(active: Boolean) {
        _isLeetModeActive.value = active
    }

    fun updateLivePreview(input: String, translated: String) {
        if (input.isNotEmpty()) {
            _livePreview.value = "$input → $translated"
        } else {
            _livePreview.value = ""
        }
    }

    fun clearLivePreview() {
        _livePreview.value = ""
    }

    fun addWordSuggestion(original: String, translated: String) {
        val currentSuggestions = _suggestions.value.toMutableList()
        currentSuggestions.add("$original → $translated")

        // Keep only last 3 suggestions
        if (currentSuggestions.size > 3) {
            currentSuggestions.removeAt(0)
        }

        _suggestions.value = currentSuggestions
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    fun showToast(message: String) {
        _toastMessage.value = message

        // Auto-clear toast after 2 seconds
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(2000)
            _toastMessage.value = null
        }
    }

    fun setSuggestedMode(mode: LeetTranslator.TranslationMode) {
        // This is a suggestion, not forced change
        // Could show a subtle indicator in UI
    }

    fun updateInputBuffer(buffer: String) {
        // Update any buffer-dependent UI states
    }

    fun onInputStarted(editorInfo: EditorInfo?) {
        // Handle input context changes
        clearSuggestions()
        clearLivePreview()
    }

    fun onInputFinished() {
        // Cleanup when input ends
        clearSuggestions()
        clearLivePreview()
    }
}