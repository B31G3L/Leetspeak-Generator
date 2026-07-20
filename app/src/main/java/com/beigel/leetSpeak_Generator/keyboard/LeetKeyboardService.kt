package com.beigel.leetSpeak_Generator.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.translation.LeetTranslator

/**
 * Eigenständige Leetspeak-Tastatur (IME). Übersetzt jeden getippten Buchstaben
 * live in den aktuell gewählten Leet-Modus und committet direkt das übersetzte
 * Zeichen ins Zielfeld — kein Umweg über Copy/Paste aus der App nötig.
 *
 * Teilt sich die Custom Leets mit der Haupt-App über [LeetManager] (liest
 * dieselben SharedPreferences "LeetSpeakProfiles"), speichert den zuletzt
 * gewählten Modus aber separat in eigenen Prefs, da die Tastatur unabhängig
 * von einer laufenden Activity/ViewModel-Instanz existiert.
 */
class LeetKeyboardService : InputMethodService() {

    private lateinit var leetManager: LeetManager
    private lateinit var prefs: SharedPreferences

    private var mode: LeetTranslator.TranslationMode = LeetTranslator.TranslationMode.SIMPLE
    private var customIndex: Int = -1
    private var isShiftOn = false
    private var isSymbolsLayer = false

    private var modeLabelView: TextView? = null
    private var keysContainer: LinearLayout? = null

    companion object {
        private const val PREFS_NAME = "LeetKeyboardPrefs"
        private const val KEY_MODE = "keyboard_mode"
        private const val KEY_CUSTOM_INDEX = "keyboard_custom_index"

        private const val MODE_SIMPLE_INT = 0
        private const val MODE_EXTENDED_INT = 1
        private const val MODE_CUSTOM_INT = 2

        private const val ROW1 = "qwertyuiop"
        private const val ROW2 = "asdfghjkl"
        private const val ROW3 = "zxcvbnm"

        private const val SYM_ROW1 = "1234567890"
        private const val SYM_ROW2 = "@#\$_&-+()/"
        private const val SYM_ROW3 = "*\"':;!"
    }

    override fun onCreate() {
        super.onCreate()
        leetManager = LeetManager(applicationContext)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadState()
    }

    private fun loadState() {
        mode = when (prefs.getInt(KEY_MODE, MODE_SIMPLE_INT)) {
            MODE_EXTENDED_INT -> LeetTranslator.TranslationMode.EXTENDED
            MODE_CUSTOM_INT -> LeetTranslator.TranslationMode.CUSTOM
            else -> LeetTranslator.TranslationMode.SIMPLE
        }
        customIndex = prefs.getInt(KEY_CUSTOM_INDEX, -1)
    }

    private fun saveState() {
        val modeInt = when (mode) {
            LeetTranslator.TranslationMode.SIMPLE -> MODE_SIMPLE_INT
            LeetTranslator.TranslationMode.EXTENDED -> MODE_EXTENDED_INT
            LeetTranslator.TranslationMode.CUSTOM -> MODE_CUSTOM_INT
        }
        prefs.edit()
            .putInt(KEY_MODE, modeInt)
            .putInt(KEY_CUSTOM_INDEX, customIndex)
            .apply()
    }

    override fun onCreateInputView(): View = buildKeyboard()

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Falls Custom Leets zwischenzeitlich in der Haupt-App gelöscht wurden:
        // Index gegen die aktuelle Liste absichern, sonst zurück auf Simple.
        val leets = leetManager.leets.value
        if (mode == LeetTranslator.TranslationMode.CUSTOM && customIndex !in leets.indices) {
            mode = LeetTranslator.TranslationMode.SIMPLE
            customIndex = -1
            saveState()
        }
        isShiftOn = false
        isSymbolsLayer = false
        refreshModeLabel()
        rebuildKeys()
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    // ---------------------------------------------------------------------
    // Modus-Auswahl (Simple → Extended → Custom Leets → zurück zu Simple)
    // ---------------------------------------------------------------------

    private fun cycleMode(forward: Boolean) {
        val customCount = leetManager.leets.value.size
        val total = 2 + customCount
        var flatIndex = when (mode) {
            LeetTranslator.TranslationMode.SIMPLE -> 0
            LeetTranslator.TranslationMode.EXTENDED -> 1
            LeetTranslator.TranslationMode.CUSTOM -> 2 + customIndex.coerceAtLeast(0)
        }
        flatIndex = if (forward) (flatIndex + 1) % total else (flatIndex - 1 + total) % total

        when (flatIndex) {
            0 -> { mode = LeetTranslator.TranslationMode.SIMPLE; customIndex = -1 }
            1 -> { mode = LeetTranslator.TranslationMode.EXTENDED; customIndex = -1 }
            else -> { mode = LeetTranslator.TranslationMode.CUSTOM; customIndex = flatIndex - 2 }
        }
        saveState()
        refreshModeLabel()
    }

    private fun currentCustomLeet(): CustomLeet? =
        if (mode == LeetTranslator.TranslationMode.CUSTOM)
            leetManager.leets.value.getOrNull(customIndex)
        else null

    private fun currentModeName(): String = when (mode) {
        LeetTranslator.TranslationMode.SIMPLE -> getString(R.string.leet_option_simple_name)
        LeetTranslator.TranslationMode.EXTENDED -> getString(R.string.leet_option_extended_name)
        LeetTranslator.TranslationMode.CUSTOM ->
            currentCustomLeet()?.name ?: getString(R.string.leet_option_simple_name)
    }

    private fun refreshModeLabel() {
        modeLabelView?.text = currentModeName()
    }

    // ---------------------------------------------------------------------
    // Zeicheneingabe
    // ---------------------------------------------------------------------

    private fun commitChar(char: Char) {
        val effective = if (isShiftOn) char.uppercaseChar() else char.lowercaseChar()
        val translated = LeetTranslator.translateChar(effective, mode, currentCustomLeet())
        currentInputConnection?.commitText(translated, 1)
        if (isShiftOn) {
            isShiftOn = false
            rebuildKeys()
        }
    }

    /** Ziffern/Symbole werden unverändert committet (kein Buchstabe → keine Übersetzung nötig). */
    private fun commitLiteral(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun handleBackspace() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun handleSpace() {
        currentInputConnection?.commitText(" ", 1)
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val action = (currentInputEditorInfo?.imeOptions ?: EditorInfo.IME_ACTION_NONE) and EditorInfo.IME_MASK_ACTION
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun toggleShift() {
        isShiftOn = !isShiftOn
        rebuildKeys()
    }

    private fun toggleSymbols() {
        isSymbolsLayer = !isSymbolsLayer
        rebuildKeys()
    }

    // ---------------------------------------------------------------------
    // UI-Aufbau (klassische Views statt Compose — InputMethodService-Fenster
    // haben kein natürliches LifecycleOwner/ViewModelStoreOwner, das Compose
    // bräuchte; mit klassischen Views entfällt dieses Setup komplett)
    // ---------------------------------------------------------------------

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    /** Gleichmäßige Tastenabstände: LayoutParams mit Gewicht + kleinem Rundum-Margin. */
    private fun keyParams(weight: Float): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
            val m = dp(2)
            setMargins(m, m, m, m)
        }

    private fun buildKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_background))
            setPadding(dp(6), dp(6), dp(6), dp(6))
        }

        // Modus-Auswahl-Leiste: ‹ Modusname ›
        val modeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(40)).apply {
                bottomMargin = dp(4)
            }
        }
        val prevButton = createSpecialKey("‹") { cycleMode(forward = false) }
        val nextButton = createSpecialKey("›") { cycleMode(forward = true) }
        val label = TextView(this).apply {
            text = currentModeName()
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_accent))
            textSize = 14f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = keyParams(1f)
        }
        modeLabelView = label
        modeRow.addView(prevButton, LinearLayout.LayoutParams(dp(44), ViewGroup.LayoutParams.MATCH_PARENT))
        modeRow.addView(label)
        modeRow.addView(nextButton, LinearLayout.LayoutParams(dp(44), ViewGroup.LayoutParams.MATCH_PARENT))
        root.addView(modeRow)

        val keys = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        keysContainer = keys
        root.addView(keys)

        rebuildKeys()
        return root
    }

    private fun rebuildKeys() {
        val container = keysContainer ?: return
        container.removeAllViews()

        if (isSymbolsLayer) {
            container.addView(buildLiteralRow(SYM_ROW1))
            container.addView(buildLiteralRow(SYM_ROW2))
            container.addView(
                buildRow(
                    left = createSpecialKey("ABC") { toggleSymbols() },
                    leftWeight = 1.5f,
                    middleChars = SYM_ROW3,
                    middleIsLiteral = true,
                    right = createSpecialKey("⌫") { handleBackspace() },
                    rightWeight = 1.5f
                )
            )
        } else {
            container.addView(buildLetterRow(ROW1))
            container.addView(buildLetterRow(ROW2, sideInsetWeight = 0.5f))
            val shiftKey = createSpecialKey("⇧") { toggleShift() }.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@LeetKeyboardService,
                        if (isShiftOn) R.color.keyboard_accent else R.color.keyboard_key_special_bg
                    )
                )
                setTextColor(
                    ContextCompat.getColor(
                        this@LeetKeyboardService,
                        if (isShiftOn) R.color.keyboard_accent_text else R.color.keyboard_key_special_text
                    )
                )
            }
            container.addView(
                buildRow(
                    left = shiftKey,
                    leftWeight = 1.5f,
                    middleChars = ROW3,
                    middleIsLiteral = false,
                    right = createSpecialKey("⌫") { handleBackspace() },
                    rightWeight = 1.5f
                )
            )
        }

        // Untere Reihe: 123/ABC-Umschalter, Komma, Leertaste, Punkt, Enter
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)).apply {
                topMargin = dp(4)
            }
        }
        val symToggle = createSpecialKey(if (isSymbolsLayer) "ABC" else "?123") { toggleSymbols() }
        val comma = createKey(",") { commitLiteral(",") }
        val space = createSpecialKey(getString(R.string.keyboard_space_label)) { handleSpace() }
        val dot = createKey(".") { commitLiteral(".") }
        val enter = createSpecialKey("⏎") { handleEnter() }.apply {
            setBackgroundColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_accent))
            setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_accent_text))
        }

        bottomRow.addView(symToggle, keyParams(1.2f))
        bottomRow.addView(comma, keyParams(1f))
        bottomRow.addView(space, keyParams(3.6f))
        bottomRow.addView(dot, keyParams(1f))
        bottomRow.addView(enter, keyParams(1.2f))
        container.addView(bottomRow)
    }

    /** Reihe aus reinen Buchstabentasten (werden live übersetzt). */
    private fun buildLetterRow(chars: String, sideInsetWeight: Float = 0f): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)).apply {
                topMargin = dp(4)
            }
            if (sideInsetWeight > 0f) {
                addView(View(this@LeetKeyboardService), LinearLayout.LayoutParams(0, 0, sideInsetWeight))
            }
            for (c in chars) {
                addView(
                    createKey(c.toString()) { commitChar(c) },
                    keyParams(1f)
                )
            }
            if (sideInsetWeight > 0f) {
                addView(View(this@LeetKeyboardService), LinearLayout.LayoutParams(0, 0, sideInsetWeight))
            }
        }
    }

    /** Reihe aus reinen Ziffern-/Symboltasten (werden 1:1 committet, keine Leet-Übersetzung). */
    private fun buildLiteralRow(chars: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)).apply {
                topMargin = dp(4)
            }
            for (c in chars) {
                addView(
                    createKey(c.toString()) { commitLiteral(c.toString()) },
                    keyParams(1f)
                )
            }
        }
    }

    /** Reihe: Spezialtaste links + Buchstaben/Symbole in der Mitte + Spezialtaste rechts (z.B. Shift + ZXCVBNM + Backspace). */
    private fun buildRow(
        left: View,
        leftWeight: Float,
        middleChars: String,
        middleIsLiteral: Boolean,
        right: View,
        rightWeight: Float
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)).apply {
                topMargin = dp(4)
            }
            addView(left, keyParams(leftWeight))
            for (c in middleChars) {
                val key = if (middleIsLiteral) {
                    createKey(c.toString()) { commitLiteral(c.toString()) }
                } else {
                    createKey(c.toString()) { commitChar(c) }
                }
                addView(key, keyParams(1f))
            }
            addView(right, keyParams(rightWeight))
        }
    }

    private fun createKey(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        textSize = 18f
        setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_text))
        setBackgroundColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_bg))
        isClickable = true
        isFocusable = true
        includeFontPadding = false
        setOnClickListener { onClick() }
    }

    private fun createSpecialKey(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        textSize = 16f
        setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_special_text))
        setBackgroundColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_special_bg))
        isClickable = true
        isFocusable = true
        includeFontPadding = false
        setOnClickListener { onClick() }
    }
}
