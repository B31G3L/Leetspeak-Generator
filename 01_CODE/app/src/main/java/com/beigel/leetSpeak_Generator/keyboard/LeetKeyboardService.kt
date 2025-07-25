package com.beigel.leetSpeak_Generator.keyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.keyboard.engine.LiveTranslationEngine
import com.beigel.leetSpeak_Generator.keyboard.data.FavoriteDataObserver
import com.beigel.leetSpeak_Generator.keyboard.ui.LeetKeyboardUI
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.*

/**
 * 🎹 LEETSPEAK KEYBOARD - Custom IME Service (COMPLETELY FIXED)
 *
 * CRITICAL FIXES:
 * - NO CONSTRUCTOR PARAMETERS (Android requirement)
 * - Manual dependency creation in onCreate()
 * - Zero-argument constructor guaranteed
 * - All dependencies created after service instantiation
 */
class LeetKeyboardService : InputMethodService(),
    ViewModelStoreOwner, LifecycleOwner {

    // CRITICAL: NO CONSTRUCTOR PARAMETERS!
    // Android System creates this with zero-argument constructor

    // Lifecycle management
    private val _lifecycle = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = _lifecycle

    // ViewModelStore für Compose
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    // Dependencies - CREATED IN onCreate(), NOT CONSTRUCTOR
    private var translationEngine: LiveTranslationEngine? = null
    private var favoriteDataObserver: FavoriteDataObserver? = null
    private var keyboardViewModel: LeetKeyboardViewModel? = null

    // UI State
    private var composeView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Keyboard State
    private var currentLeetMode = LeetTranslator.TranslationMode.SIMPLE
    private var isLeetModeActive = true
    private val inputBuffer = StringBuilder()

    override fun onCreate() {
        super.onCreate()

        android.util.Log.d("LeetKeyboard", "Service onCreate() called")

        _lifecycle.currentState = Lifecycle.State.CREATED

        try {
            // CRITICAL: Create ALL dependencies here, NOT in constructor
            initializeComponents()
            startFavoriteObserver()
            _lifecycle.currentState = Lifecycle.State.STARTED

            android.util.Log.d("LeetKeyboard", "Service initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error in onCreate", e)
        }
    }

    private fun initializeComponents() {
        // Create dependencies manually - NO INJECTION
        translationEngine = LiveTranslationEngine()
        favoriteDataObserver = FavoriteDataObserver(this)
        keyboardViewModel = LeetKeyboardViewModel(
            translationEngine!!,
            favoriteDataObserver!!
        )

        android.util.Log.d("LeetKeyboard", "Components initialized")
    }

    private fun startFavoriteObserver() {
        try {
            favoriteDataObserver?.startObserving { newLeet ->
                serviceScope.launch {
                    try {
                        translationEngine?.updateFavoriteLeet(newLeet)
                        currentLeetMode = if (newLeet != null) {
                            LeetTranslator.TranslationMode.CUSTOM
                        } else {
                            LeetTranslator.TranslationMode.SIMPLE
                        }
                        keyboardViewModel?.updateCurrentMode(currentLeetMode, newLeet)
                    } catch (e: Exception) {
                        android.util.Log.e("LeetKeyboard", "Error updating favorite", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error starting favorite observer", e)
        }
    }

    override fun onCreateInputView(): View? {
        return try {
            android.util.Log.d("LeetKeyboard", "Creating input view")

            _lifecycle.currentState = Lifecycle.State.RESUMED

            ComposeView(this).apply {
                composeView = this
                setViewTreeLifecycleOwner(this@LeetKeyboardService)
                setViewTreeViewModelStoreOwner(this@LeetKeyboardService)

                setContent {
                    keyboardViewModel?.let { viewModel ->
                        LeetKeyboardUI(
                            viewModel = viewModel,
                            onKeyPress = ::handleKeyPress,
                            onSpecialAction = ::handleSpecialAction,
                            onModeToggle = ::toggleLeetMode,
                            onSettingsOpen = ::openMainApp
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error creating input view", e)
            null
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)

        try {
            inputBuffer.clear()
            updateContextualSettings(attribute)
            keyboardViewModel?.onInputStarted(attribute)
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error in onStartInput", e)
        }
    }

    private fun updateContextualSettings(editorInfo: EditorInfo?) {
        try {
            val packageName = editorInfo?.packageName?.toString() ?: ""

            when {
                packageName.contains("whatsapp") -> {
                    keyboardViewModel?.setSuggestedMode(LeetTranslator.TranslationMode.SIMPLE)
                }
                packageName.contains("discord") || packageName.contains("game") -> {
                    keyboardViewModel?.setSuggestedMode(LeetTranslator.TranslationMode.EXTENDED)
                }
                editorInfo?.inputType?.and(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) != 0 -> {
                    isLeetModeActive = false
                }
                else -> {
                    isLeetModeActive = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error updating contextual settings", e)
        }
    }

    private fun handleKeyPress(key: String) {
        try {
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

            keyboardViewModel?.updateInputBuffer(inputBuffer.toString())
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error handling key press", e)
        }
    }

    private fun handleLetterKey(char: Char, inputConnection: InputConnection) {
        try {
            inputBuffer.append(char)

            if (isLeetModeActive) {
                val translatedChar = translationEngine?.translateChar(char, currentLeetMode) ?: char.toString()
                inputConnection.commitText(translatedChar, 1)

                translationEngine?.let { engine ->
                    keyboardViewModel?.updateLivePreview(
                        inputBuffer.toString(),
                        engine.translateWord(inputBuffer.toString())
                    )
                }
            } else {
                inputConnection.commitText(char.toString(), 1)
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error handling letter key", e)
        }
    }

    private fun handleSpaceKey(inputConnection: InputConnection) {
        try {
            if (inputBuffer.isNotEmpty() && isLeetModeActive) {
                val currentWord = inputBuffer.toString()
                val translatedWord = translationEngine?.translateWord(currentWord) ?: currentWord
                keyboardViewModel?.addWordSuggestion(currentWord, translatedWord)
            }

            inputBuffer.clear()
            inputConnection.commitText(" ", 1)
            keyboardViewModel?.clearLivePreview()
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error handling space key", e)
        }
    }

    private fun handleBackspaceKey(inputConnection: InputConnection) {
        try {
            val selectedText = inputConnection.getSelectedText(0)

            if (selectedText != null && selectedText.isNotEmpty()) {
                inputConnection.commitText("", 1)
            } else {
                inputConnection.deleteSurroundingText(1, 0)
            }

            if (inputBuffer.isNotEmpty()) {
                inputBuffer.deleteCharAt(inputBuffer.length - 1)
                keyboardViewModel?.updateInputBuffer(inputBuffer.toString())
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error handling backspace", e)
        }
    }

    private fun handleSpecialAction(action: String) {
        try {
            when (action) {
                "TOGGLE_LEET" -> toggleLeetMode()
                "QUICK_SETTINGS" -> openMainApp()
                "LEET_SIGNATURE" -> insertLeetSignature()
                "CLEAR_SUGGESTIONS" -> keyboardViewModel?.clearSuggestions()
                "SWITCH_MODE" -> cycleLeetMode()
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error handling special action", e)
        }
    }

    private fun toggleLeetMode() {
        try {
            isLeetModeActive = !isLeetModeActive
            keyboardViewModel?.setLeetModeActive(isLeetModeActive)

            if (isLeetModeActive) {
                keyboardViewModel?.showToast("🎯 Leet Mode ON")
            } else {
                keyboardViewModel?.showToast("📝 Normal Mode")
            }
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error toggling leet mode", e)
        }
    }

    private fun cycleLeetMode() {
        try {
            currentLeetMode = when (currentLeetMode) {
                LeetTranslator.TranslationMode.SIMPLE -> LeetTranslator.TranslationMode.EXTENDED
                LeetTranslator.TranslationMode.EXTENDED -> LeetTranslator.TranslationMode.CUSTOM
                LeetTranslator.TranslationMode.CUSTOM -> LeetTranslator.TranslationMode.SIMPLE
            }

            keyboardViewModel?.updateCurrentMode(currentLeetMode, translationEngine?.getCurrentCustomLeet())
            keyboardViewModel?.showToast("Mode: ${getModeDisplayName()}")
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error cycling leet mode", e)
        }
    }

    private fun getModeDisplayName(): String {
        return when (currentLeetMode) {
            LeetTranslator.TranslationMode.SIMPLE -> "Simple Leet"
            LeetTranslator.TranslationMode.EXTENDED -> "Extended Leet"
            LeetTranslator.TranslationMode.CUSTOM -> translationEngine?.getCurrentCustomLeet()?.name ?: "Custom Leet"
        }
    }

    private fun insertLeetSignature() {
        try {
            val signatures = listOf(
                "|_337 5P34K 6363|2470|2",
                "pwn3d by l33t",
                "1337 h4x0r",
                "н4ск тне ρℓαηєт"
            )

            val signature = signatures.random()
            currentInputConnection?.commitText(" $signature ", 1)
            keyboardViewModel?.showToast("🎮 Leet signature inserted!")
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error inserting signature", e)
        }
    }

    private fun openMainApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error opening main app", e)
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        try {
            inputBuffer.clear()
            keyboardViewModel?.onInputFinished()
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error in onFinishInput", e)
        }
    }

    override fun onDestroy() {
        try {
            _lifecycle.currentState = Lifecycle.State.DESTROYED
            serviceScope.cancel()
            favoriteDataObserver?.stopObserving()
            store.clear()

            android.util.Log.d("LeetKeyboard", "Service destroyed")
        } catch (e: Exception) {
            android.util.Log.e("LeetKeyboard", "Error in onDestroy", e)
        } finally {
            super.onDestroy()
        }
    }
}

/**
 * 🧠 ViewModel für Keyboard State Management
 * CONSTRUCTOR TAKES PARAMETERS - but created manually, not by Android System
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

        CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(2000)
            _toastMessage.value = null
        }
    }

    fun setSuggestedMode(mode: LeetTranslator.TranslationMode) {
        // This is a suggestion, not forced change
    }

    fun updateInputBuffer(buffer: String) {
        // Update any buffer-dependent UI states
    }

    fun onInputStarted(editorInfo: EditorInfo?) {
        clearSuggestions()
        clearLivePreview()
    }

    fun onInputFinished() {
        clearSuggestions()
        clearLivePreview()
    }
}