package com.beigel.leetSpeak_Generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.beigel.leetSpeak_Generator.compose.ComposeTestActivity
import com.beigel.leetSpeak_Generator.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.beigel.leetSpeak_Generator.compose.LeetSelectorBottomSheet
import com.beigel.leetSpeak_Generator.ui.theme.LeetspeakGeneratorTheme
import javax.inject.Inject

/**
 * Main Activity mit vollständiger Hilt Integration
 * Repository wird jetzt auch über Hilt injiziert
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator
    private var showComposeBottomSheet by mutableStateOf(false)

    // ✅ Repository wird von Hilt injiziert
    @Inject
    lateinit var repository: ProfileRepository

    // ✅ ViewModel wird von Hilt injiziert (mit Repository)
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setupUI()
        observeViewModel()
        setupComposeIntegration()
    }

    /**
     * Sets up the UI components and event listeners
     */
    private fun setupUI() {
        setupToolbar()
        setupInputHandling()
        setupButtons()
    }

    /**
     * Sets up toolbar mit Compose Test Button
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayShowTitleEnabled(false)
        }

        // Compose Test Button (temporär für Testing)
        binding.appTitle.setOnLongClickListener {
            showComposeTestDialog()
            true
        }
    }

    private fun setupComposeIntegration() {
        // Compose Content für Bottom Sheet
        val composeView = androidx.compose.ui.platform.ComposeView(this)

        // Unsichtbar zu Layout hinzufügen (für Compose State Management)
        binding.root.addView(composeView)
        composeView.visibility = android.view.View.GONE

        composeView.setContent {
            LeetspeakGeneratorTheme {
                // Bottom Sheet State Management
                if (showComposeBottomSheet) {
                    LeetSelectorBottomSheet(
                        viewModel = viewModel,
                        onDismiss = {
                            showComposeBottomSheet = false
                        }
                    )
                }
            }
        }
    }

    private fun showComposeTestDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("🚀 Compose Test")
            .setMessage("Möchten Sie den neuen Compose Screen testen?")
            .setPositiveButton("Ja") { _, _ ->
                val intent = Intent(this, ComposeTestActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Nein", null)
            .show()
    }

    /**
     * Sets up input text handling with reactive updates
     */
    private fun setupInputHandling() {
        binding.inputPlainText.addTextChangedListener { editable ->
            viewModel.handleIntent(MainIntent.UpdateInput(editable?.toString() ?: ""))
        }
    }

    /**
     * Sets up button click listeners
     */
    private fun setupButtons() {
        binding.buttonCopy.setOnClickListener {
            copyToClipboardWithFeedback()
        }

        binding.buttonLeetSelector.setOnClickListener {
            showLeetSelectorChoice()
        }

        binding.buttonPlainSelector.setOnClickListener {
            showPlainModeDialog()
        }

        // Compose Test (bestehend)
        binding.buttonPlainSelector.setOnLongClickListener {
            showComposeTestDialog()
            true
        }
    }

    private fun showLeetSelectorChoice() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Bottom Sheet Version")
            .setMessage("Welche Version möchten Sie testen?")
            .setPositiveButton("🚀 Neue (Compose)") { _, _ ->
                showComposeBottomSheet = true
            }
            .setNeutralButton("📱 Alte (XML)") { _, _ ->
                showLeetSelectorBottomSheet()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    /**
     * Observes ViewModel state changes and updates UI accordingly
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.outputText.collect { outputText ->
                binding.outputLeetText.text = outputText
                updateLayoutForOutput(outputText)
            }
        }

        lifecycleScope.launch {
            viewModel.currentModeDisplayName.collect { displayName ->
                binding.buttonLeetSelector.text = displayName
                binding.outputModeTitle.text = displayName
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                handleUiState(uiState)
            }
        }

        lifecycleScope.launch {
            viewModel.uiState
                .distinctUntilChanged { old, new -> old.isLoading == new.isLoading }
                .collect { uiState ->
                    // Show/hide loading indicator if needed
                }
        }
    }

    /**
     * Handles UI state changes (errors, success messages, etc.)
     */
    private fun handleUiState(uiState: MainUiState) {
        uiState.errorMessage?.let { message ->
            ErrorHandler.showSnackbar(this, message, Snackbar.LENGTH_LONG)
            viewModel.handleIntent(MainIntent.ClearError)
        }

        uiState.successMessage?.let { message ->
            ErrorHandler.showSnackbar(this, message, Snackbar.LENGTH_SHORT)
            viewModel.handleIntent(MainIntent.ClearSuccess)
        }
    }

    /**
     * Updates layout based on output text presence
     */
    private fun updateLayoutForOutput(outputText: String) {
        val hasOutput = outputText.isNotEmpty()

        if (hasOutput) {
            if (binding.outputSection.visibility != View.VISIBLE) {
                binding.outputSection.visibility = View.VISIBLE
                binding.divider.visibility = View.VISIBLE
                AnimationHelper.slideInFromBottom(binding.outputSection)
            }
        } else {
            binding.outputSection.visibility = View.GONE
            binding.divider.visibility = View.GONE
        }
    }

    /**
     * Shows the leet selector bottom sheet
     */
    private fun showLeetSelectorBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_leet_selector, null)

        setupBottomSheetContent(bottomSheetView, bottomSheetDialog)

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    /**
     * Sets up the content of the bottom sheet
     */
    private fun setupBottomSheetContent(view: View, dialog: BottomSheetDialog) {
        val recyclerViewLeets = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewLeets)
        val recyclerViewFavorites = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewFavorites)
        val favoritesHeader = view.findViewById<TextView>(R.id.favoritesHeader)
        val buttonAddNewLeet = view.findViewById<MaterialButton>(R.id.buttonAddNewLeet)

        recyclerViewLeets.layoutManager = LinearLayoutManager(this)
        recyclerViewFavorites.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val listener = createLeetSelectorListener(dialog)

        lifecycleScope.launch {
            viewModel.leetOptions.collect { options ->
                val adapter = LeetSelectorAdapter(options, listener)
                recyclerViewLeets.adapter = adapter
            }
        }

        lifecycleScope.launch {
            viewModel.favoriteLeetOptions.collect { favoriteOptions ->
                if (favoriteOptions.isEmpty()) {
                    favoritesHeader.visibility = View.GONE
                    recyclerViewFavorites.visibility = View.GONE
                } else {
                    favoritesHeader.visibility = View.VISIBLE
                    recyclerViewFavorites.visibility = View.VISIBLE

                    val favoritesAdapter = LeetSelectorAdapter(favoriteOptions, listener)
                    recyclerViewFavorites.adapter = favoritesAdapter
                }
            }
        }

        buttonAddNewLeet.setOnClickListener {
            dialog.dismiss()
            showNewProfileDialog()
        }
    }

    /**
     * Creates listener for leet selector
     */
    private fun createLeetSelectorListener(dialog: BottomSheetDialog): LeetSelectorAdapter.OnLeetSelectedListener {
        return object : LeetSelectorAdapter.OnLeetSelectedListener {
            override fun onLeetSelected(leetOption: LeetOption) {
                viewModel.handleIntent(MainIntent.ChangeMode(leetOption))
                dialog.dismiss()
            }

            override fun onLeetPreview(leetOption: LeetOption) {
                // Preview functionality can be implemented later
            }

            override fun onEditLeet(leetOption: LeetOption) {
                if (leetOption.isCustom) {
                    dialog.dismiss()
                    showEditProfileDialog(leetOption.customIndex)
                }
            }

            override fun onToggleFavorite(leetOption: LeetOption) {
                viewModel.handleIntent(MainIntent.ToggleFavorite(leetOption))
                AnimationHelper.pulse(dialog.findViewById(R.id.iconFavorite)!!, 1)
            }

            override fun onQuickTest(leetOption: LeetOption) {
                val preview = viewModel.generatePreview(leetOption, "Test")
                ErrorHandler.showSnackbar(this@MainActivity, "Test → $preview", Snackbar.LENGTH_SHORT)
            }

            override fun onShowTable(leetOption: LeetOption) {
                dialog.dismiss()
                showTableDialog(leetOption)
            }
        }
    }

    /**
     * Shows dialog for creating new profile
     */
    private fun showNewProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_comprehensive_edit, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setupProfileEditDialog(dialogView, dialog, isNewProfile = true)
        dialog.show()
    }

    private fun showEditProfileDialog(profileIndex: Int) {
        lifecycleScope.launch {
            val profile = viewModel.profiles.value.getOrNull(profileIndex)
            if (profile != null) {
                val dialogView = layoutInflater.inflate(R.layout.dialog_comprehensive_edit, null)
                val dialog = MaterialAlertDialogBuilder(this@MainActivity)
                    .setView(dialogView)
                    .create()

                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                setupProfileEditDialog(dialogView, dialog, isNewProfile = false, existingProfile = profile, profileIndex = profileIndex)
                dialog.show()
            }
        }
    }

    private fun setupProfileEditDialog(
        dialogView: View,
        dialog: androidx.appcompat.app.AlertDialog,
        isNewProfile: Boolean,
        existingProfile: CustomLeet? = null,
        profileIndex: Int = -1
    ) {
        val editTextProfileName = dialogView.findViewById<TextInputEditText>(R.id.editTextProfileName)
        val selectedIcon = dialogView.findViewById<ImageView>(R.id.selectedIcon)
        val editTable = dialogView.findViewById<TableLayout>(R.id.editTable)
        val buttonCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCancel)
        val buttonSave = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonSave)

        var selectedIconResId = existingProfile?.iconResId ?: R.drawable.ic_custom_mode

        if (existingProfile != null) {
            editTextProfileName.setText(existingProfile.name)
            selectedIcon.setImageResource(existingProfile.iconResId)
        } else {
            selectedIcon.setImageResource(selectedIconResId)
        }

        selectedIcon.setOnClickListener {
            showIconSelectorDialog { newIconResId ->
                selectedIconResId = newIconResId
                selectedIcon.setImageResource(newIconResId)
            }
        }

        val tableEditFields = buildEditTable(editTable, existingProfile)

        buttonCancel.setOnClickListener { dialog.dismiss() }

        buttonSave.setOnClickListener {
            val profileName = editTextProfileName.text?.toString()?.trim()
                ?: getString(R.string.default_custom_name)

            saveProfileChanges(
                profileName,
                selectedIconResId,
                tableEditFields,
                isNewProfile,
                profileIndex,
                dialog
            )
        }

        editTextProfileName.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextProfileName, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun buildEditTable(editTable: TableLayout, existingProfile: CustomLeet?): Array<Array<EditText?>> {
        val tableEditFields = Array(13) { Array<EditText?>(2) { null } }
        val cellPadding = resources.getDimensionPixelSize(R.dimen.table_cell_padding)

        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

        for (i in 0 until 13) {
            val row = TableRow(this)

            val leftPlain = createTableTextView(alphabet[i].toString(), cellPadding)
            row.addView(leftPlain)

            val leftLeet = createTableEditText(
                existingProfile?.getTranslation(alphabet[i].toString()) ?: alphabet[i].toString(),
                cellPadding
            )
            tableEditFields[i][0] = leftLeet
            row.addView(leftLeet)

            val rightPlain = createTableTextView(alphabet[i + 13].toString(), cellPadding)
            row.addView(rightPlain)

            val rightLeet = createTableEditText(
                existingProfile?.getTranslation(alphabet[i + 13].toString()) ?: alphabet[i + 13].toString(),
                cellPadding
            )
            tableEditFields[i][1] = rightLeet
            row.addView(rightLeet)

            editTable.addView(row)
        }

        return tableEditFields
    }

    private fun createTableTextView(text: String, padding: Int): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(padding, padding, padding, padding)
            setTextColor(getColor(R.color.text_primary))
            typeface = resources.getFont(R.font.raleway)
            gravity = android.view.Gravity.CENTER
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
        }
    }

    private fun createTableEditText(text: String, padding: Int): EditText {
        return EditText(this).apply {
            setText(text)
            setTextColor(getColor(R.color.text_primary))
            typeface = resources.getFont(R.font.raleway)
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.edit_text_background)
            setPadding(padding, padding, padding, padding)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
        }
    }

    private fun saveProfileChanges(
        profileName: String,
        selectedIconResId: Int,
        tableEditFields: Array<Array<EditText?>>,
        isNewProfile: Boolean,
        profileIndex: Int,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        try {
            val translations = mutableMapOf<String, String>()
            val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

            for (i in 0 until 13) {
                val leftPlain = alphabet[i].toString()
                val leftLeet = tableEditFields[i][0]?.text?.toString() ?: leftPlain
                translations[leftPlain] = leftLeet

                val rightPlain = alphabet[i + 13].toString()
                val rightLeet = tableEditFields[i][1]?.text?.toString() ?: rightPlain
                translations[rightPlain] = rightLeet
            }

            if (isNewProfile) {
                viewModel.handleIntent(
                    MainIntent.CreateProfile(profileName, selectedIconResId, false)
                )
            } else {
                val updatedProfile = CustomLeet(profileName, selectedIconResId)
                updatedProfile.setTranslations(translations)
                viewModel.handleIntent(
                    MainIntent.UpdateProfile(profileIndex, updatedProfile)
                )
            }

            dialog.dismiss()

        } catch (e: Exception) {
            ErrorHandler.handleError(this, e, "Error saving profile", ErrorHandler.ErrorSeverity.ERROR)
        }
    }

    private fun showIconSelectorDialog(onIconSelected: (Int) -> Unit) {
        val iconDialog = IconSelectorDialog(this, onIconSelected)
        iconDialog.show()
    }

    private fun showTableDialog(leetOption: LeetOption) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_translation_table, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        val tableTitle = dialogView.findViewById<TextView>(R.id.tableTitle)
        val tableLayout = dialogView.findViewById<TableLayout>(R.id.translationTable)
        val buttonClose = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonClose)

        tableTitle.text = "Übersetzungstabelle - ${leetOption.name}"
        buildTableContent(tableLayout, leetOption)

        buttonClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun buildTableContent(tableLayout: TableLayout, leetOption: LeetOption) {
        tableLayout.removeAllViews()

        val cellPadding = resources.getDimensionPixelSize(R.dimen.table_cell_padding)
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

        for (i in 0 until 13) {
            val row = TableRow(this)

            val leftPlain = createTableTextView(alphabet[i].toString(), cellPadding)
            row.addView(leftPlain)

            val leftLeet = createTableTextView(
                getTranslatedCharForOption(alphabet[i], leetOption),
                cellPadding
            )
            row.addView(leftLeet)

            val rightPlain = createTableTextView(alphabet[i + 13].toString(), cellPadding)
            row.addView(rightPlain)

            val rightLeet = createTableTextView(
                getTranslatedCharForOption(alphabet[i + 13], leetOption),
                cellPadding
            )
            row.addView(rightLeet)

            tableLayout.addView(row)
        }
    }

    private fun getTranslatedCharForOption(char: Char, leetOption: LeetOption): String {
        val mode = when (leetOption.mode) {
            LeetManager.MODE_SIMPLE -> LeetTranslator.TranslationMode.SIMPLE
            LeetManager.MODE_EXTENDED -> LeetTranslator.TranslationMode.EXTENDED
            LeetManager.MODE_CUSTOM -> LeetTranslator.TranslationMode.CUSTOM
            else -> LeetTranslator.TranslationMode.SIMPLE
        }

        val profile = if (leetOption.isCustom && leetOption.customIndex >= 0) {
            viewModel.profiles.value.getOrNull(leetOption.customIndex)
        } else null

        return LeetTranslator.translateChar(char, mode, profile)
    }

    private fun showPlainModeDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Plain Text Modus")
            .setMessage("Plain Text ist der Eingabemodus für normalen Text.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun copyToClipboardWithFeedback() {
        val text = binding.outputLeetText.text.toString()
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Leetspeak Text", text)
            clipboard.setPrimaryClip(clip)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, 120))
            }

            AnimationHelper.pulse(binding.buttonCopy, 1)
            viewModel.handleIntent(MainIntent.CopyToClipboard)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        lifecycleScope.launch {
            val currentMode = viewModel.currentMode.value
            val currentProfile = viewModel.currentProfile.value
            val currentIndex = repository.getCurrentProfileIndex()

            val editItem = menu.findItem(R.id.action_edit)
            val deleteItem = menu.findItem(R.id.action_delete)
            val favoriteItem = menu.findItem(R.id.action_favorite)

            val isCustomMode = currentMode == LeetTranslator.TranslationMode.CUSTOM
            val hasCustomProfile = isCustomMode && currentProfile != null

            editItem?.isVisible = hasCustomProfile
            deleteItem?.isVisible = hasCustomProfile

            if (favoriteItem != null) {
                val isFavorite = repository.isFavorite(
                    when (currentMode) {
                        LeetTranslator.TranslationMode.SIMPLE -> LeetManager.MODE_SIMPLE
                        LeetTranslator.TranslationMode.EXTENDED -> LeetManager.MODE_EXTENDED
                        LeetTranslator.TranslationMode.CUSTOM -> LeetManager.MODE_CUSTOM
                    },
                    currentIndex
                )

                favoriteItem.setIcon(
                    if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                )
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                lifecycleScope.launch {
                    val currentMode = viewModel.currentMode.value
                    val currentIndex = repository.getCurrentProfileIndex()

                    val leetOption = when (currentMode) {
                        LeetTranslator.TranslationMode.SIMPLE -> LeetOption.createSimple()
                        LeetTranslator.TranslationMode.EXTENDED -> LeetOption.createExtended()
                        LeetTranslator.TranslationMode.CUSTOM -> {
                            val profile = viewModel.currentProfile.value
                            if (profile != null) {
                                LeetOption.createCustom(profile, currentIndex)
                            } else null
                        }
                    }

                    leetOption?.let { option ->
                        viewModel.handleIntent(MainIntent.ToggleFavorite(option))
                    }
                }
                true
            }
            R.id.action_edit -> {
                lifecycleScope.launch {
                    val currentIndex = repository.getCurrentProfileIndex()
                    showEditProfileDialog(currentIndex)
                }
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmDialog() {
        lifecycleScope.launch {
            val currentProfile = viewModel.currentProfile.value
            if (currentProfile != null) {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.delete_confirm)
                    .setMessage(getString(R.string.delete_confirm_message_named, currentProfile.name))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        val currentIndex = repository.getCurrentProfileIndex()
                        viewModel.handleIntent(MainIntent.DeleteProfile(currentIndex))
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.cleanup()
    }
}