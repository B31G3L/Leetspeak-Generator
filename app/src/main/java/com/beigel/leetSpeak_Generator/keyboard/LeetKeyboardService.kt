package com.beigel.leetSpeak_Generator.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.beigel.leetSpeak_Generator.R
import com.beigel.leetSpeak_Generator.data.CustomLeet
import com.beigel.leetSpeak_Generator.data.ThemePreferences
import com.beigel.leetSpeak_Generator.manager.LeetManager
import com.beigel.leetSpeak_Generator.translation.LeetTranslator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    private lateinit var themePreferences: ThemePreferences
    private lateinit var prefs: SharedPreferences
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var mode: LeetTranslator.TranslationMode = LeetTranslator.TranslationMode.SIMPLE
    private var customIndex: Int = -1
    private var isShiftOn = false
    private var isCapsLock = false
    private var lastShiftTapTime = 0L
    private var isSymbolsLayer = false

    /**
     * Aktuelle Buchstaben-Anordnung. Wird aus den App-Einstellungen (DataStore)
     * gespiegelt, damit eine Änderung in den Settings sofort greift, ohne dass
     * die Tastatur neu gestartet werden muss.
     */
    private var layout: KeyboardLayout = KeyboardLayout.DEFAULT

    private var modeLabelView: TextView? = null
    private var keysContainer: LinearLayout? = null

    companion object {
        private const val PREFS_NAME = "LeetKeyboardPrefs"
        private const val KEY_MODE = "keyboard_mode"
        private const val KEY_CUSTOM_INDEX = "keyboard_custom_index"

        private const val MODE_SIMPLE_INT = 0
        private const val MODE_EXTENDED_INT = 1
        private const val MODE_CUSTOM_INT = 2

        private const val DOUBLE_TAP_MS = 350L

        private const val SYM_ROW1 = "1234567890"
        private const val SYM_ROW2 = "@#\$_&-+()/"
        private const val SYM_ROW3 = "*\"':;!?"

        /** Long-Press-Varianten für Umlaute/Akzente, analog zu Gboard. Deckt DE
         * (ä ö ü ß) sowie Akzentzeichen für die übrigen App-Sprachen FR/ES/IT ab. */
        private val ACCENT_VARIANTS: Map<Char, String> = mapOf(
            'a' to "äàáâã",
            'e' to "éèêë",
            'i' to "íìîï",
            'o' to "öòóôõ",
            'u' to "üùúû",
            's' to "ß",
            'c' to "çć",
            'n' to "ñ"
        )

        private const val LONG_PRESS_MS = 350L
        private const val REPEAT_INITIAL_DELAY_MS = 400L
        private const val REPEAT_INTERVAL_MS = 50L
    }

    override fun onCreate() {
        super.onCreate()
        leetManager = LeetManager(applicationContext)
        themePreferences = ThemePreferences(applicationContext)
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadState()

        // Layout-Wahl dauerhaft beobachten statt nur einmal lesen: Wird das
        // Layout in den App-Einstellungen umgestellt, während die Tastatur im
        // Hintergrund lebt, baut sich die Tastenfläche direkt neu auf.
        serviceScope.launch {
            themePreferences.keyboardLayout.collectLatest { key ->
                val newLayout = KeyboardLayout.fromKey(key)
                if (newLayout != layout) {
                    layout = newLayout
                    rebuildKeys()
                }
            }
        }

        // LeetManager lädt die Custom Leets asynchron aus den SharedPreferences.
        // Falls die Tastatur schneller angezeigt wird als dieser Ladevorgang
        // fertig ist, wären Custom Leets in der Modus-Auswahl unsichtbar.
        // Deshalb hier explizit warten und danach — falls die Tastatur zu dem
        // Zeitpunkt schon sichtbar ist — Modus-Anzeige und Tasten auffrischen.
        serviceScope.launch {
            leetManager.awaitLoaded()
            refreshModeLabel()
            rebuildKeys()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
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
        applyNavigationBarIconAppearance()
        isShiftOn = false
        isCapsLock = false
        isSymbolsLayer = false

        // Bei jedem Öffnen der Tastatur frisch aus den SharedPreferences laden:
        // Die App und die Tastatur halten getrennte LeetManager-Instanzen, die
        // sich nicht automatisch über neue/geänderte/gelöschte Custom Leets
        // informieren (siehe LeetManager.reload()). Ohne das hier würden neu
        // erstellte Leets erst nach einem Neustart der Tastatur auftauchen.
        serviceScope.launch {
            leetManager.reload()
            // Falls Custom Leets zwischenzeitlich in der Haupt-App gelöscht wurden:
            // Index gegen die aktuelle Liste absichern, sonst zurück auf Simple.
            val leets = leetManager.leets.value
            if (mode == LeetTranslator.TranslationMode.CUSTOM && customIndex !in leets.indices) {
                mode = LeetTranslator.TranslationMode.SIMPLE
                customIndex = -1
                saveState()
            }
            refreshModeLabel()
            rebuildKeys()
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Falls der System-Dark-Mode bei offener Tastatur umgeschaltet wird,
        // sonst würden die System-Icons (Pfeil/Globus) auf falschem Kontrast bleiben.
        applyNavigationBarIconAppearance()
    }

    /**
     * Sagt dem System, ob die vom OS selbst gezeichneten Icons in der
     * Navigationsleiste (Einklappen-Pfeil, Tastatur-wechseln-Globus) hell
     * oder dunkel sein sollen. Ohne das übernimmt Android eine Standard-
     * Annahme, die im Light Mode zu hellen (kaum sichtbaren) Icons auf dem
     * hellen Tastatur-Hintergrund führt. [keyboard_background] hat eigene
     * values-night-Farben, die dem System-Dark-Mode folgen — also richten wir
     * uns nach genau diesem Flag statt nach einem eigenen App-Theme-Override,
     * da die Tastatur (anders als die Activities) keinen Zugriff auf die in
     * ThemePreferences gespeicherte manuelle Theme-Wahl hat.
     */
    private fun applyNavigationBarIconAppearance() {
        val dialogWindow = window?.window ?: return
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val controller = WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
        controller.isAppearanceLightNavigationBars = !isNightMode
    }

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
        LeetTranslator.TranslationMode.SIMPLE -> localizedString(R.string.leet_option_simple_name)
        LeetTranslator.TranslationMode.EXTENDED -> localizedString(R.string.leet_option_extended_name)
        LeetTranslator.TranslationMode.CUSTOM ->
            currentCustomLeet()?.name ?: localizedString(R.string.leet_option_simple_name)
    }

    /**
     * Löst Strings über den in der App eingestellten Sprach-Override auf, statt
     * einfach über die System-Sprache. Die In-App-Sprachauswahl setzt
     * [AppCompatDelegate.setApplicationLocales], was unter Android 13 automatisch
     * für Activities greift — die Tastatur läuft aber als eigenständiger
     * [android.app.Service], der diese Aktivitäts-spezifische Context-Anpassung
     * nicht automatisch mitbekommt und sonst auf die Systemsprache zurückfallen
     * würde. AppCompatDelegate.getApplicationLocales() funktioniert dagegen
     * prozessweit auch ohne Activity, deshalb bauen wir uns hier selbst einen
     * passend lokalisierten Context.
     */
    private fun localizedString(resId: Int): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val locale = if (appLocales.isEmpty) null else appLocales[0]
        val localizedContext = if (locale != null) {
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            createConfigurationContext(config)
        } else {
            this
        }
        return localizedContext.getString(resId)
    }

    private fun refreshModeLabel() {
        modeLabelView?.text = currentModeName()
    }

    // ---------------------------------------------------------------------
    // Zeicheneingabe
    // ---------------------------------------------------------------------

    private fun commitChar(char: Char) {
        val effective = if (isShiftOn || isCapsLock) char.uppercaseChar() else char.lowercaseChar()
        val translated = LeetTranslator.translateChar(effective, mode, currentCustomLeet())
        currentInputConnection?.commitText(translated, 1)
        // Einmaliger Shift (nicht Caps-Lock) schaltet sich nach einem Zeichen wieder ab —
        // genau wie bei Gboard.
        if (isShiftOn && !isCapsLock) {
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

    /**
     * Gboard-typisches Shift-Verhalten:
     * - Einzeltipp: einmalige Großschreibung fürs nächste Zeichen
     * - Doppeltipp (innerhalb 350ms): Caps-Lock an/aus
     * - Tipp bei aktivem Caps-Lock: schaltet Caps-Lock wieder aus
     */
    private fun handleShiftTap() {
        val now = System.currentTimeMillis()
        val isDoubleTap = now - lastShiftTapTime < DOUBLE_TAP_MS
        lastShiftTapTime = now

        when {
            isCapsLock -> {
                isCapsLock = false
                isShiftOn = false
            }
            isDoubleTap -> {
                isCapsLock = true
                isShiftOn = true
            }
            else -> {
                isShiftOn = !isShiftOn
            }
        }
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

    /**
     * Baut eine abgerundete "Tastenkappe" im Gboard-Stil: normaler Zustand in
     * [normalColor], beim Antippen kurz heller/dunkler eingefärbt in
     * [pressedColor] für sofortiges Touch-Feedback — genau wie bei Gboard, wo
     * jede Taste beim Drücken sichtbar aufleuchtet.
     */
    private fun keyBackground(normalColorRes: Int, pressedColorRes: Int, radiusDp: Int = 6): StateListDrawable {
        val radius = dp(radiusDp).toFloat()
        val normal = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(ContextCompat.getColor(this@LeetKeyboardService, normalColorRes))
        }
        val pressed = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(ContextCompat.getColor(this@LeetKeyboardService, pressedColorRes))
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun buildKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_background))
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        // Ohne das hier zeichnet Android die Tastatur bis unter die System-
        // Navigationsleiste/Geste-Leiste (Edge-to-Edge ist inzwischen Standard),
        // wodurch die unterste Tastenreihe teilweise dahinter verschwindet.
        // Zusätzlich zum reinen Inset-Wert noch etwas Puffer drauf, damit genug
        // Abstand zu den System-Icons (Einklappen-Pfeil, Tastatur-wechseln-Globus)
        // bleibt, die Android dort selbst einblendet.
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val extraBuffer = dp(20)
            view.setPadding(dp(8), dp(8), dp(8), dp(8) + navBarInset + extraBuffer)
            insets
        }

        // Modus-Auswahl-Leiste: ‹ Modusname ›
        val modeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)).apply {
                bottomMargin = dp(6)
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
                    right = createRepeatingSpecialKey("⌫") { handleBackspace() },
                    rightWeight = 1.5f
                )
            )
        } else {
            val slots = layout.maxRowLength
            container.addView(buildLetterRow(layout.row1, totalSlots = slots))
            container.addView(buildLetterRow(layout.row2, totalSlots = slots))
            val shiftKey = createSpecialKey(if (isCapsLock) "⇪" else "⇧") { handleShiftTap() }.apply {
                textSize = 20f
                val isActive = isShiftOn || isCapsLock
                background = keyBackground(
                    if (isActive) R.color.keyboard_accent else R.color.keyboard_key_special_bg,
                    if (isActive) R.color.keyboard_accent else R.color.keyboard_key_pressed_bg
                )
                setTextColor(
                    ContextCompat.getColor(
                        this@LeetKeyboardService,
                        if (isActive) R.color.keyboard_accent_text else R.color.keyboard_key_special_text
                    )
                )
            }
            // Shift und Backspace füllen den Platz auf, den die dritte Reihe im
            // gemeinsamen Raster frei lässt — so bleiben die Tasten aller Reihen
            // gleich breit, egal ob das Layout 6 (AZERTY) oder 9 (Dvorak)
            // Buchstaben in der untersten Reihe hat.
            val sideWeight = 1.5f + (slots - layout.row3.length).coerceAtLeast(0) / 2f
            container.addView(
                buildRow(
                    left = shiftKey,
                    leftWeight = sideWeight,
                    middleChars = layout.row3,
                    middleIsLiteral = false,
                    right = createRepeatingSpecialKey("⌫") { handleBackspace() },
                    rightWeight = sideWeight
                )
            )
        }

        // Untere Reihe: 123/ABC-Umschalter, Komma, Leertaste, Punkt, Enter
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply {
                topMargin = dp(6)
            }
        }
        val symToggle = createSpecialKey(if (isSymbolsLayer) "ABC" else "?123") { toggleSymbols() }
        val comma = createSpecialKey(",") { commitLiteral(",") }
        val space = createSpecialKey(localizedString(R.string.keyboard_space_label)) { handleSpace() }.apply {
            textSize = 13f
            setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_preview_text))
            // Leertaste als durchgehende Pille (volle Rundung) — typisches Gboard-Detail.
            background = keyBackground(R.color.keyboard_key_special_bg, R.color.keyboard_key_pressed_bg, radiusDp = 20)
        }
        val dot = createSpecialKey(".") { commitLiteral(".") }
        val enter = createSpecialKey("⏎") { handleEnter() }.apply {
            textSize = 20f
            background = keyBackground(R.color.keyboard_accent, R.color.keyboard_accent)
            setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_accent_text))
        }

        bottomRow.addView(symToggle, keyParams(1.2f))
        bottomRow.addView(comma, keyParams(1f))
        bottomRow.addView(space, keyParams(3.6f))
        bottomRow.addView(dot, keyParams(1f))
        bottomRow.addView(enter, keyParams(1.2f))
        container.addView(bottomRow)
    }

    /** Zeigt Buchstaben groß an, wenn Shift oder Caps-Lock aktiv ist — wie bei Gboard. */
    private fun letterLabel(c: Char): String =
        if (isShiftOn || isCapsLock) c.uppercaseChar().toString() else c.toString()

    /**
     * Reihe aus reinen Buchstabentasten (werden live übersetzt). [totalSlots] gibt
     * das gemeinsame Raster aller Reihen an: Ist die Reihe kürzer, wird die
     * Differenz links und rechts als leerer Platzhalter verteilt, damit die
     * Tastenbreite über alle Reihen hinweg identisch bleibt.
     */
    private fun buildLetterRow(chars: String, totalSlots: Int = chars.length): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply {
                topMargin = dp(6)
            }
            val sideInsetWeight = (totalSlots - chars.length).coerceAtLeast(0) / 2f
            if (sideInsetWeight > 0f) {
                addView(View(this@LeetKeyboardService), LinearLayout.LayoutParams(0, 0, sideInsetWeight))
            }
            for (c in chars) {
                addView(createLetterKey(c), keyParams(1f))
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
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply {
                topMargin = dp(6)
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
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply {
                topMargin = dp(6)
            }
            addView(left, keyParams(leftWeight))
            for (c in middleChars) {
                val key = if (middleIsLiteral) {
                    createKey(c.toString()) { commitLiteral(c.toString()) }
                } else {
                    createLetterKey(c)
                }
                addView(key, keyParams(1f))
            }
            addView(right, keyParams(rightWeight))
        }
    }

    private fun createKey(label: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        textSize = 20f
        setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_text))
        // Wie beim Original-Keyboard: Buchstaben schweben ohne sichtbare Box,
        // nur beim Antippen leuchtet die Taste kurz auf.
        background = keyBackground(android.R.color.transparent, R.color.keyboard_key_pressed_bg)
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
        background = keyBackground(R.color.keyboard_key_special_bg, R.color.keyboard_key_pressed_bg)
        isClickable = true
        isFocusable = true
        includeFontPadding = false
        setOnClickListener { onClick() }
    }

    // ---------------------------------------------------------------------
    // Buchstabentaste mit Long-Press-Popup für Umlaute/Akzente (ä ö ü ß é ñ …)
    // ---------------------------------------------------------------------

    /**
     * Buchstabentaste, die bei normalem Tap [commitChar] auslöst. Ist für den
     * Buchstaben eine Akzent-Variante in [ACCENT_VARIANTS] hinterlegt, öffnet
     * ein Long-Press (analog zu Gboard) ein Auswahl-Popup über der Taste;
     * Ziehen zu einer Variante und Loslassen committet diese, Loslassen
     * außerhalb des Popups bricht ab.
     */
    private fun createLetterKey(c: Char): TextView {
        val key = TextView(this).apply {
            text = letterLabel(c)
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_text))
            background = keyBackground(android.R.color.transparent, R.color.keyboard_key_pressed_bg)
            isClickable = true
            isFocusable = true
            includeFontPadding = false
        }

        val variants = ACCENT_VARIANTS[c.lowercaseChar()]
        if (variants.isNullOrEmpty()) {
            key.setOnClickListener { commitChar(c) }
            return key
        }

        attachAccentPopup(key, c, variants)
        return key
    }

    private fun attachAccentPopup(key: TextView, baseChar: Char, variants: String) {
        var popup: PopupWindow? = null
        var itemViews: List<TextView> = emptyList()
        var highlightedIndex = -1
        var longPressFired = false
        var popupLeftOnScreen = 0
        var popupItemWidth = 0

        fun setHighlight(index: Int) {
            if (index == highlightedIndex) return
            itemViews.getOrNull(highlightedIndex)?.setBackgroundColor(
                ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_special_bg)
            )
            itemViews.getOrNull(index)?.setBackgroundColor(
                ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_accent)
            )
            highlightedIndex = index
        }

        fun dismissPopup() {
            popup?.dismiss()
            popup = null
            itemViews = emptyList()
            highlightedIndex = -1
        }

        val longPressRunnable = Runnable {
            longPressFired = true
            key.isPressed = false

            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val views = variants.map { variantChar ->
                TextView(this).apply {
                    text = variantChar.toString()
                    gravity = Gravity.CENTER
                    textSize = 20f
                    setTextColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_special_text))
                    setBackgroundColor(ContextCompat.getColor(this@LeetKeyboardService, R.color.keyboard_key_special_bg))
                    setPadding(dp(14), dp(10), dp(14), dp(10))
                    row.addView(this)
                }
            }
            itemViews = views

            row.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val popupWidth = row.measuredWidth
            val popupHeight = row.measuredHeight

            val newPopup = PopupWindow(row, popupWidth, popupHeight, false).apply {
                isTouchable = false
                isOutsideTouchable = false
                elevation = dp(6).toFloat()
            }
            popup = newPopup

            val loc = IntArray(2)
            key.getLocationOnScreen(loc)
            val x = (loc[0] + key.width / 2 - popupWidth / 2).coerceAtLeast(0)
            val y = loc[1] - popupHeight - dp(4)
            newPopup.showAtLocation(key.rootView, Gravity.NO_GRAVITY, x, y)

            popupLeftOnScreen = x
            popupItemWidth = if (views.isNotEmpty()) popupWidth / views.size else popupWidth
        }

        key.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    longPressFired = false
                    key.isPressed = true
                    key.postDelayed(longPressRunnable, LONG_PRESS_MS)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (longPressFired && popup != null && popupItemWidth > 0) {
                        val relativeX = event.rawX - popupLeftOnScreen
                        val index = (relativeX / popupItemWidth).toInt()
                        if (index in variants.indices) {
                            setHighlight(index)
                        } else {
                            setHighlight(-1)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    key.removeCallbacks(longPressRunnable)
                    key.isPressed = false
                    if (longPressFired) {
                        val index = highlightedIndex
                        dismissPopup()
                        if (index in variants.indices) {
                            commitChar(variants[index])
                        }
                        // Loslassen außerhalb aller Varianten: Eingabe wird abgebrochen,
                        // kein Zeichen committet (Standardverhalten von Akzent-Popups).
                    } else {
                        commitChar(baseChar)
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    key.removeCallbacks(longPressRunnable)
                    key.isPressed = false
                    dismissPopup()
                    true
                }
                else -> false
            }
        }
    }

    // ---------------------------------------------------------------------
    // Spezialtaste mit Key-Repeat bei gedrückt gehalten (z. B. Backspace) —
    // wie bei Gboard: erster Tastendruck sofort, danach wiederholtes Auslösen
    // nach kurzer Anfangsverzögerung, solange die Taste gehalten wird.
    // ---------------------------------------------------------------------

    private fun createRepeatingSpecialKey(label: String, action: () -> Unit): TextView {
        val key = createSpecialKey(label) {}

        val repeatRunnable = object : Runnable {
            override fun run() {
                action()
                key.postDelayed(this, REPEAT_INTERVAL_MS)
            }
        }
        val startRepeatRunnable = Runnable { key.post(repeatRunnable) }

        key.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    key.isPressed = true
                    action()
                    key.postDelayed(startRepeatRunnable, REPEAT_INITIAL_DELAY_MS)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    key.isPressed = false
                    key.removeCallbacks(startRepeatRunnable)
                    key.removeCallbacks(repeatRunnable)
                    true
                }
                else -> false
            }
        }
        return key
    }
}
