package com.beigel.leetSpeak_Generator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beigel.leetSpeak_Generator.databinding.ActivityMainBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int SIMPLE = ProfileManager.MODE_SIMPLE;
    private static final int EXTENDED = ProfileManager.MODE_EXTENDED;
    private static final int CUSTOM = ProfileManager.MODE_CUSTOM;
    private static final int ABOUT = ProfileManager.MODE_ABOUT;

    private int activeMode = SIMPLE;

    // ViewBinding - nur für ActivityMain
    private ActivityMainBinding binding;

    // Repository Pattern
    private ProfileRepository profileRepository;

    // UI Components
    private LinearLayout inputSection;
    private LinearLayout outputSection;
    private View divider;
    private MaterialButton buttonLeetSelector;
    private MaterialButton buttonPlainSelector;
    private TextView outputModeTitle;
    private Vibrator vibrator;

    // Entferne alle Tabellen-Methoden und Variablen
    private EditText[][] editableFields = new EditText[13][2];
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding Setup
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Repository initialisieren
        profileRepository = new ProfileRepository(this);

        // UI Setup
        initializeViews();

        // Favoriten laden - jetzt asynchron!
        loadFavoriteMode();
    }

    private void showTableDialog(LeetOption leetOption) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_translation_table, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tableTitle = dialogView.findViewById(R.id.tableTitle);
        TableLayout tableLayout = dialogView.findViewById(R.id.translationTable);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);

        // Set title based on leet option
        String title = "Übersetzungstabelle - " + leetOption.getName();
        tableTitle.setText(title);

        // Build table content
        buildTableContent(tableLayout, leetOption);

        buttonClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void buildTableContent(TableLayout tableLayout, LeetOption leetOption) {
        tableLayout.removeAllViews();

        int cellPadding = (int) getResources().getDimension(R.dimen.table_cell_padding);
        int colorEven = getResources().getColor(R.color.gray_light, getTheme());
        int colorOdd = getResources().getColor(android.R.color.transparent, getTheme());

        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        for (int i = 0; i < 13; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? colorEven : colorOdd);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                row.setForeground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ripple_effect, getTheme()));
            }

            // Left side (letters 0-12)
            TextView leftPlain = createTableTextView(String.valueOf(alphabet[i]), cellPadding);
            row.addView(leftPlain);

            TextView leftLeet = createTableTextView(getTranslatedCharForOption(alphabet[i], leetOption), cellPadding);
            row.addView(leftLeet);

            // Right side (letters 13-25)
            TextView rightPlain = createTableTextView(String.valueOf(alphabet[i + 13]), cellPadding);
            row.addView(rightPlain);

            TextView rightLeet = createTableTextView(getTranslatedCharForOption(alphabet[i + 13], leetOption), cellPadding);
            row.addView(rightLeet);

            tableLayout.addView(row);
        }
    }

    private String getTranslatedCharForOption(char c, LeetOption leetOption) {
        LeetTranslator.TranslationMode mode;
        CustomProfile profile = null;

        switch (leetOption.getMode()) {
            case SIMPLE:
                mode = LeetTranslator.TranslationMode.SIMPLE;
                break;
            case EXTENDED:
                mode = LeetTranslator.TranslationMode.EXTENDED;
                break;
            case CUSTOM:
                mode = LeetTranslator.TranslationMode.CUSTOM;
                if (leetOption.getCustomIndex() >= 0) {
                    List<CustomProfile> profiles = profileRepository.getProfiles();
                    if (leetOption.getCustomIndex() < profiles.size()) {
                        profile = profiles.get(leetOption.getCustomIndex());
                    }
                }
                break;
            default:
                mode = LeetTranslator.TranslationMode.SIMPLE;
        }

        return LeetTranslator.translateChar(c, mode, profile);
    }


    private void initializeViews() {
        // Toolbar Setup ohne Drawer
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Vibrator für Haptic Feedback
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // View-Referenzen
        inputSection = findViewById(R.id.input_section);
        outputSection = findViewById(R.id.output_section);
        divider = findViewById(R.id.divider);
        buttonLeetSelector = findViewById(R.id.buttonLeetSelector);
        buttonPlainSelector = findViewById(R.id.buttonPlainSelector);
        outputModeTitle = findViewById(R.id.outputModeTitle);

        // Event Listeners mit ViewBinding
        binding.inputPlainText.addTextChangedListener(simpleTextWatcher);
        binding.buttonCopy.setOnClickListener(v -> copyToClipboardWithFeedback());

        // Neue Button-Event Listeners
        buttonLeetSelector.setOnClickListener(v -> showLeetSelectorBottomSheet());
        buttonPlainSelector.setOnClickListener(v -> showPlainModeDialog());

        // Initial: Output-Sektion ausblenden
        updateLayoutForOutput("");
        updateButtonTexts();
        updateOutputModeTitle();
    }

    private void showPlainModeDialog() {
        // Für zukünftige Erweiterungen - momentan zeigen wir nur eine Info
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Plain Text Modus")
                .setMessage("Plain Text ist der Eingabemodus für normalen Text. " +
                        "Hier können Sie verschiedene Eingabeoptionen konfigurieren.")
                .setPositiveButton("OK", null)
                .show();
    }


    private List<LeetOption> createLeetOptionsList() {
        List<LeetOption> options = new ArrayList<>();

        // Simple Leet
        options.add(new LeetOption(
                SIMPLE,
                getString(R.string.simple),
                "Einfache Leetspeak-Übersetzung",
                R.drawable.ic_simple_mode,
                false,
                -1,
                activeMode == SIMPLE,
                profileRepository.getProfileManager().isFavorite(SIMPLE, 0)
        ));

        // Extended Leet
        options.add(new LeetOption(
                EXTENDED,
                getString(R.string.extended),
                "Erweiterte Leetspeak-Übersetzung",
                R.drawable.ic_extended_mode,
                false,
                -1,
                activeMode == EXTENDED,
                profileRepository.getProfileManager().isFavorite(EXTENDED, 0)
        ));

        // Custom Leets
        List<CustomProfile> customProfiles = profileRepository.getProfiles();
        for (int i = 0; i < customProfiles.size(); i++) {
            CustomProfile profile = customProfiles.get(i);
            options.add(new LeetOption(
                    CUSTOM,
                    profile.getName(),
                    "Benutzerdefiniertes Leet",
                    profile.getIconResId(),
                    true,
                    i,
                    activeMode == CUSTOM && profileRepository.getCurrentProfileIndex() == i,
                    profileRepository.getProfileManager().isFavorite(CUSTOM, i)
            ));
        }

        return options;
    }

    private void selectLeet(LeetOption leetOption) {
        activeMode = leetOption.getMode();

        if (leetOption.isCustom()) {
            profileRepository.setCurrentProfileIndex(leetOption.getCustomIndex());
        }

        updateButtonTexts();
        updateAppTitle();
        updateOutput();
        updateOutputModeTitle();
        invalidateOptionsMenu();
    }

    private void updateButtonTexts() {
        // Plain Button bleibt konstant
        buttonPlainSelector.setText("Plain");

        // Leet Button je nach Modus
        String leetName;
        switch (activeMode) {
            case SIMPLE:
                leetName = getString(R.string.simple);
                break;
            case EXTENDED:
                leetName = getString(R.string.extended);
                break;
            case CUSTOM:
                CustomProfile currentProfile = profileRepository.getCurrentProfile();
                leetName = currentProfile != null ? currentProfile.getName() : getString(R.string.custom);
                break;
            default:
                leetName = getString(R.string.simple);
        }

        buttonLeetSelector.setText(leetName);
    }

    private void updateOutputModeTitle() {
        if (outputModeTitle != null) {
            String leetName;
            switch (activeMode) {
                case SIMPLE:
                    leetName = getString(R.string.simple);
                    break;
                case EXTENDED:
                    leetName = getString(R.string.extended);
                    break;
                case CUSTOM:
                    CustomProfile currentProfile = profileRepository.getCurrentProfile();
                    leetName = currentProfile != null ? currentProfile.getName() : getString(R.string.custom);
                    break;
                default:
                    leetName = getString(R.string.simple);
            }
            outputModeTitle.setText(leetName);
        }
    }

    private void loadFavoriteMode() {
        profileRepository.loadFavoriteMode(new ProfileRepository.FavoriteModeCallback() {
            @Override
            public void onSuccess(ProfileRepository.FavoriteModeResult result) {
                switch (result.getMode()) {
                    case ProfileManager.MODE_SIMPLE:
                        setActiveMode(SIMPLE);
                        break;
                    case ProfileManager.MODE_EXTENDED:
                        setActiveMode(EXTENDED);
                        break;
                    case ProfileManager.MODE_CUSTOM:
                        setActiveMode(CUSTOM);
                        profileRepository.setCurrentProfileIndex(result.getCustomIndex());
                        break;
                }

                // UI nach dem Laden aktualisieren
                updateOutput();
                updateButtonTexts();
                updateOutputModeTitle();
            }

            @Override
            public void onError(Exception e) {
                ErrorHandler.handleError(MainActivity.this, e,
                        "Fehler beim Laden der Favoriten",
                        ErrorHandler.ErrorSeverity.WARNING);

                // Fallback zu Simple Mode
                setActiveMode(SIMPLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem saveItem = menu.findItem(R.id.action_save);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);

        if (editItem != null && saveItem != null && deleteItem != null) {
            boolean isCustomMode = activeMode == CUSTOM;
            boolean hasCustomProfile = isCustomMode && profileRepository.getCurrentProfile() != null;

            editItem.setVisible(hasCustomProfile);
            deleteItem.setVisible(hasCustomProfile);
            saveItem.setVisible(false);
        }

        MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        if (favoriteItem != null) {
            favoriteItem.setVisible(activeMode != ABOUT);

            if (activeMode == CUSTOM && profileRepository.getCurrentProfile() == null) {
                favoriteItem.setVisible(false);
            } else {
                int customIndex = activeMode == CUSTOM ? profileRepository.getCurrentProfileIndex() : 0;
                boolean isFavorite = profileRepository.getProfileManager().isFavorite(activeMode, customIndex);

                favoriteItem.setIcon(isFavorite
                        ? R.drawable.ic_favorite
                        : R.drawable.ic_favorite_border);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_favorite) {
            toggleFavorite();
            return true;
        } else if (itemId == R.id.action_edit) {
            switchToEditMode();
            return true;
        } else if (itemId == R.id.action_save) {
            saveCustomTranslations();
            return true;
        } else if (itemId == R.id.action_delete) {
            showDeleteProfileConfirmDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite() {
        int customIndex = 0;
        if (activeMode == CUSTOM) {
            customIndex = profileRepository.getCurrentProfileIndex();
        }

        profileRepository.toggleFavorite(activeMode, customIndex,
                new ProfileRepository.FavoriteToggleCallback() {
                    @Override
                    public void onToggleComplete(ProfileRepository.FavoriteToggleResult result) {
                        // UI aktualisieren
                        invalidateOptionsMenu();

                        // Benutzer-Feedback
                        String modeName = getModeDisplayName(result.getMode(), result.getCustomIndex());

                        int messageResId = result.isNowFavorite()
                                ? R.string.favorite_added
                                : R.string.favorite_removed;

                        ErrorHandler.showSnackbar(MainActivity.this,
                                getString(messageResId, modeName),
                                Snackbar.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(Exception e) {
                        ErrorHandler.handleError(MainActivity.this, e,
                                "Fehler beim Ändern des Favoriten-Status",
                                ErrorHandler.ErrorSeverity.WARNING);
                    }
                });
    }

    private void toggleFavoriteFromBottomSheet(LeetOption leetOption, LeetSelectorAdapter adapter) {
        int mode = leetOption.getMode();
        int customIndex = leetOption.isCustom() ? leetOption.getCustomIndex() : 0;

        profileRepository.toggleFavorite(mode, customIndex,
                new ProfileRepository.FavoriteToggleCallback() {
                    @Override
                    public void onToggleComplete(ProfileRepository.FavoriteToggleResult result) {
                        invalidateOptionsMenu();

                        String modeName = leetOption.getName();
                        int messageResId = result.isNowFavorite()
                                ? R.string.favorite_added
                                : R.string.favorite_removed;

                        ErrorHandler.showSnackbar(MainActivity.this,
                                getString(messageResId, modeName),
                                Snackbar.LENGTH_SHORT);

                        // Update the option object
                        leetOption.setFavorite(result.isNowFavorite());

                        // Update RecyclerView
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e) {
                        ErrorHandler.handleError(MainActivity.this, e,
                                "Fehler beim Ändern des Favoriten-Status",
                                ErrorHandler.ErrorSeverity.WARNING);
                    }
                });
    }

    private String getModeDisplayName(int mode, int customIndex) {
        switch (mode) {
            case SIMPLE:
                return getString(R.string.simple);
            case EXTENDED:
                return getString(R.string.extended);
            case CUSTOM:
                CustomProfile profile = profileRepository.getCurrentProfile();
                return profile != null ? profile.getName() : getString(R.string.custom);
            default:
                return "Unknown";
        }
    }


    private void createNewProfileWithRepository(String name, int iconResId) {
        Map<String, String> translations = new HashMap<>();

        LeetTranslator.TranslationMode currentTranslationMode = getCurrentTranslationMode();

        for (char c = 'A'; c <= 'Z'; c++) {
            String plainChar = String.valueOf(c);
            String leetChar = LeetTranslator.translateChar(c, currentTranslationMode, null);
            translations.put(plainChar, leetChar);
        }

        ProfileRepository.ProfileCreationRequest request =
                new ProfileRepository.ProfileCreationRequest(name, iconResId, translations);

        profileRepository.createProfile(request, new ProfileRepository.ProfileOperationCallback() {
            @Override
            public void onComplete(ProfileOperationResult result) {
                if (result.isSuccess()) {
                    setActiveMode(CUSTOM);
                    profileRepository.setCurrentProfileIndex(result.getProfileIndex());

                    updateButtonTexts();
                    updateAppTitle();
                    updateOutputModeTitle();
                    updateOutputModeTitle();

                    ErrorHandler.showSnackbar(MainActivity.this,
                            getString(R.string.profile_created, name),
                            Snackbar.LENGTH_SHORT);

                } else {
                    ErrorHandler.handleError(MainActivity.this,
                            result.getException(),
                            result.getMessage(),
                            ErrorHandler.ErrorSeverity.ERROR);
                }
            }
        });
    }

    private void showDeleteProfileConfirmDialog() {
        CustomProfile currentProfile = profileRepository.getCurrentProfile();
        if (currentProfile == null) {
            ErrorHandler.showSnackbar(this, getString(R.string.no_custom_profiles),
                    Snackbar.LENGTH_SHORT);
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.delete_confirm)
                .setMessage(getString(R.string.delete_confirm_message_named, currentProfile.getName()))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    deleteProfileWithRepository(profileRepository.getCurrentProfileIndex());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteProfileWithRepository(int profileIndex) {
        profileRepository.deleteProfile(profileIndex, new ProfileRepository.ProfileOperationCallback() {
            @Override
            public void onComplete(ProfileOperationResult result) {
                if (result.isSuccess()) {
                    ProfileRepository.ProfileDeletionResult deletionResult = result.getDeletionResult();

                    if (deletionResult.wasLastProfile()) {
                        setActiveMode(SIMPLE);
                        ErrorHandler.showSnackbar(MainActivity.this,
                                getString(R.string.last_profile_deleted),
                                Snackbar.LENGTH_SHORT);
                    } else {
                        if (deletionResult.wasFavorite()) {
                            ErrorHandler.showSnackbar(MainActivity.this,
                                    getString(R.string.favorite_deleted),
                                    Snackbar.LENGTH_LONG);
                        } else {
                            ErrorHandler.showSnackbar(MainActivity.this,
                                    getString(R.string.profile_deleted),
                                    Snackbar.LENGTH_SHORT);
                        }
                    }

                    updateButtonTexts();
                    updateAppTitle();

                } else {
                    ErrorHandler.handleError(MainActivity.this,
                            result.getException(),
                            result.getMessage(),
                            ErrorHandler.ErrorSeverity.ERROR);
                }
            }
        });
    }

    private void setActiveMode(int mode) {
        if (mode == CUSTOM && !profileRepository.hasProfiles()) {
            ErrorHandler.showSnackbar(this, getString(R.string.no_custom_profiles),
                    Snackbar.LENGTH_SHORT);
            return;
        }

        activeMode = mode;

        updateAppTitle();
        updateButtonTexts();
        updateOutput();
        updateOutputModeTitle();
        invalidateOptionsMenu();
    }

    private void updateAppTitle() {
        String title = getString(R.string.app_name);
        TextView toolbarTitle = findViewById(R.id.appTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setText(title);
        }
    }

    private void updateLayoutForOutput(String outputText) {
        boolean hasOutput = outputText != null && !outputText.trim().isEmpty();

        if (hasOutput) {
            // Text vorhanden: Beide Sektionen anzeigen (50/50 Split)
            LinearLayout.LayoutParams inputParams = (LinearLayout.LayoutParams) inputSection.getLayoutParams();
            inputParams.weight = 1;
            inputSection.setLayoutParams(inputParams);

            outputSection.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);

            // Smooth Animation
            if (outputSection.getVisibility() != View.VISIBLE) {
                AnimationHelper.slideInFromBottom(outputSection);
            }
        } else {
            // Kein Text: Nur Input-Sektion (100% Höhe)
            outputSection.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);

            LinearLayout.LayoutParams inputParams = (LinearLayout.LayoutParams) inputSection.getLayoutParams();
            inputParams.weight = 1;
            inputSection.setLayoutParams(inputParams);
        }
    }

    private void updateOutput() {
        String input = binding.inputPlainText.getText().toString();

        LeetTranslator.TranslationMode mode = getCurrentTranslationMode();
        CustomProfile currentProfile = (mode == LeetTranslator.TranslationMode.CUSTOM)
                ? profileRepository.getCurrentProfile()
                : null;

        String output = LeetTranslator.translate(input, mode, currentProfile);
        binding.outputLeetText.setText(output);

        // Layout entsprechend anpassen
        updateLayoutForOutput(output);
    }

    private LeetTranslator.TranslationMode getCurrentTranslationMode() {
        switch (activeMode) {
            case SIMPLE:
                return LeetTranslator.TranslationMode.SIMPLE;
            case EXTENDED:
                return LeetTranslator.TranslationMode.EXTENDED;
            case CUSTOM:
                return LeetTranslator.TranslationMode.CUSTOM;
            default:
                return LeetTranslator.TranslationMode.SIMPLE;
        }
    }

    private String getTranslatedChar(char c) {
        LeetTranslator.TranslationMode mode = getCurrentTranslationMode();
        CustomProfile currentProfile = (mode == LeetTranslator.TranslationMode.CUSTOM)
                ? profileRepository.getCurrentProfile()
                : null;

        return LeetTranslator.translateChar(c, mode, currentProfile);
    }

    private TextView createTableTextView(String text, int padding) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        textView.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        return textView;
    }

    private EditText createTableEditText(String text, int padding) {
        EditText editText = new EditText(this);
        editText.setText(text);
        editText.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        editText.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
        editText.setGravity(Gravity.CENTER);
        editText.setBackgroundResource(R.drawable.edit_text_background);
        editText.setPadding(padding, padding, padding, padding);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        return editText;
    }

    private void switchToEditMode() {
        if (activeMode != CUSTOM) return;

        invalidateOptionsMenu();
    }

    private void saveCustomTranslations() {
        if (!isEditMode) return;

        CustomProfile currentProfile = profileRepository.getCurrentProfile();
        Map<String, String> translations = new HashMap<>();

        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        // Save left column (letters 0-12)
        for (int i = 0; i < 13; i++) {
            String plainChar = String.valueOf(alphabet[i]);
            String leetChar = editableFields[i][0].getText().toString();
            translations.put(plainChar, leetChar);
        }

        // Save right column (letters 13-25)
        for (int i = 0; i < 13; i++) {
            String plainChar = String.valueOf(alphabet[i + 13]);
            String leetChar = editableFields[i][1].getText().toString();
            translations.put(plainChar, leetChar);
        }

        currentProfile.setTranslations(translations);

        profileRepository.updateProfile(profileRepository.getCurrentProfileIndex(), currentProfile,
                new ProfileRepository.ProfileOperationCallback() {
                    @Override
                    public void onComplete(ProfileOperationResult result) {
                        if (result.isSuccess()) {
                            isEditMode = false;
                            updateOutput();
                            invalidateOptionsMenu();

                            ErrorHandler.showSnackbar(MainActivity.this,
                                    getString(R.string.save_success),
                                    Snackbar.LENGTH_SHORT);
                        } else {
                            ErrorHandler.handleError(MainActivity.this,
                                    result.getException(),
                                    result.getMessage(),
                                    ErrorHandler.ErrorSeverity.ERROR);
                        }
                    }
                });
    }

    // Ersetze die showNewProfileDialog() Methode in MainActivity.java mit dieser:

    private void showNewProfileDialog() {
        CustomProfile currentProfile = profileRepository.getCurrentProfile();

        if (currentProfile == null) {
            ErrorHandler.showSnackbar(this, getString(R.string.no_custom_profiles_available),
                    Snackbar.LENGTH_SHORT);
            return;
        }

        // Erstelle Fullscreen Dialog
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comprehensive_edit, null);

        // Setze das Layout als Content View
        builder.setView(dialogView);

        // Erstelle Dialog und mache ihn Fullscreen
        AlertDialog dialog = builder.create();

        // Fullscreen Dialog Setup
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            // Entferne Standard Dialog Padding/Margin
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // UI Elemente finden
        TextInputEditText editTextProfileName = dialogView.findViewById(R.id.editTextProfileName);
        ImageView selectedIcon = dialogView.findViewById(R.id.selectedIcon);
        TableLayout editTable = dialogView.findViewById(R.id.editTable);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        // Profil-Daten setzen
        editTextProfileName.setText(currentProfile.getName());

        int iconResId = currentProfile.getIconResId();
        if (iconResId != 0) {
            selectedIcon.setImageResource(iconResId);
        }

        final int[] selectedIconResId = {iconResId};

        // Icon Click Listener
        selectedIcon.setOnClickListener(v -> {
            IconSelectorDialog iconDialog = new IconSelectorDialog(
                    MainActivity.this,
                    (newIconResId) -> {
                        selectedIconResId[0] = newIconResId;
                        selectedIcon.setImageResource(newIconResId);
                    },
                    selectedIconResId[0]
            );
            iconDialog.show();
        });

        // Tabelle erstellen (gleiche Logik wie vorher)
        EditText[][] tableEditFields = new EditText[13][2];
        buildEditTable(editTable, tableEditFields, currentProfile);

        // Button Listeners
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            saveProfileChanges(currentProfile, editTextProfileName, selectedIconResId[0],
                    tableEditFields, dialog);
        });

        // Dialog anzeigen
        dialog.show();

        // Fokus setzen
        editTextProfileName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editTextProfileName, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // Hilfsmethode für Tabellenerstellung
    private void buildEditTable(TableLayout editTable, EditText[][] tableEditFields, CustomProfile currentProfile) {
        int cellPadding = (int) getResources().getDimension(R.dimen.table_cell_padding);
        int colorEven = getResources().getColor(R.color.gray_light, getTheme());
        int colorOdd = getResources().getColor(android.R.color.transparent, getTheme());

        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        for (int i = 0; i < 13; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? colorEven : colorOdd);

            // Left side (letters 0-12)
            TextView leftPlain = createTableTextView(String.valueOf(alphabet[i]), cellPadding);
            row.addView(leftPlain);

            EditText leftLeet = createTableEditText(
                    currentProfile.getTranslation(String.valueOf(alphabet[i])), cellPadding);
            tableEditFields[i][0] = leftLeet;
            row.addView(leftLeet);

            // Right side (letters 13-25)
            TextView rightPlain = createTableTextView(String.valueOf(alphabet[i + 13]), cellPadding);
            row.addView(rightPlain);

            EditText rightLeet = createTableEditText(
                    currentProfile.getTranslation(String.valueOf(alphabet[i + 13])), cellPadding);
            tableEditFields[i][1] = rightLeet;
            row.addView(rightLeet);

            editTable.addView(row);
        }
    }

    // Hilfsmethode zum Speichern
    private void saveProfileChanges(CustomProfile currentProfile, TextInputEditText editTextProfileName,
                                    int selectedIconResId, EditText[][] tableEditFields, AlertDialog dialog) {
        String profileName = editTextProfileName.getText().toString().trim();
        if (profileName.isEmpty()) {
            profileName = getString(R.string.default_custom_name);
        }

        try {
            currentProfile.setName(profileName);
            currentProfile.setIconResId(selectedIconResId);

            // Speichere alle Übersetzungen
            char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
            for (int i = 0; i < 13; i++) {
                String plainChar = String.valueOf(alphabet[i]);
                String leetChar = tableEditFields[i][0].getText().toString();
                currentProfile.setTranslation(plainChar, leetChar);

                plainChar = String.valueOf(alphabet[i + 13]);
                leetChar = tableEditFields[i][1].getText().toString();
                currentProfile.setTranslation(plainChar, leetChar);
            }

            // Repository Update
            profileRepository.updateProfile(profileRepository.getCurrentProfileIndex(), currentProfile,
                    new ProfileRepository.ProfileOperationCallback() {
                        @Override
                        public void onComplete(ProfileOperationResult result) {
                            if (result.isSuccess()) {
                                updateButtonTexts();
                                updateOutput();
                                updateOutputModeTitle();

                                ErrorHandler.showSnackbar(MainActivity.this,
                                        getString(R.string.all_changes_saved),
                                        Snackbar.LENGTH_SHORT);
                            } else {
                                ErrorHandler.handleError(MainActivity.this,
                                        result.getException(),
                                        result.getMessage(),
                                        ErrorHandler.ErrorSeverity.ERROR);
                            }
                        }
                    });

        } catch (Exception e) {
            ErrorHandler.handleError(this, e, "Fehler beim Aktualisieren des Profils",
                    ErrorHandler.ErrorSeverity.ERROR);
        }

        dialog.dismiss();
    }

    private void editCustomProfile(int profileIndex) {
        profileRepository.setCurrentProfileIndex(profileIndex);
    }

    // Vereinfachter TextWatcher ohne Counter und Undo/Redo
    private final TextWatcher simpleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            updateOutput();
        }
    };

    private void copyToClipboardWithFeedback() {
        String text = binding.outputLeetText.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Leetspeak Text", text);
        clipboard.setPrimaryClip(clip);

        // Enhanced feedback
        if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, 120));
        }

        // Animate copy button
        AnimationHelper.pulse(binding.buttonCopy, 1);

        ErrorHandler.showSnackbar(this, getString(R.string.copy_success), Snackbar.LENGTH_SHORT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cleanup
        if (profileRepository != null) {
            profileRepository.cleanup();
        }

        // ViewBinding cleanup
        binding = null;
    }


    private void showLeetSelectorBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_leet_selector, null);

        RecyclerView recyclerViewLeets = bottomSheetView.findViewById(R.id.recyclerViewLeets);
        RecyclerView recyclerViewFavorites = bottomSheetView.findViewById(R.id.recyclerViewFavorites);
        TextView favoritesHeader = bottomSheetView.findViewById(R.id.favoritesHeader);
        MaterialButton buttonAddNewLeet = bottomSheetView.findViewById(R.id.buttonAddNewLeet);

        // Setup für alle Leets
        recyclerViewLeets.setLayoutManager(new LinearLayoutManager(this));

        // Setup für Favoriten
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Adapter-Referenz für Updates
        final LeetSelectorAdapter[] mainAdapterHolder = new LeetSelectorAdapter[1];
        final LeetSelectorAdapter[] favoritesAdapterHolder = new LeetSelectorAdapter[1];

        // Listener für beide Listen
        LeetSelectorAdapter.OnLeetSelectedListener listener = new LeetSelectorAdapter.OnLeetSelectedListener() {
            @Override
            public void onLeetSelected(LeetOption leetOption) {
                selectLeet(leetOption);
                bottomSheetDialog.dismiss();
            }

            @Override
            public void onLeetPreview(LeetOption leetOption) {
                // Nicht mehr benötigt
            }

            @Override
            public void onEditLeet(LeetOption leetOption) {
                if (leetOption.isCustom()) {
                    editCustomProfile(leetOption.getCustomIndex());
                }
                bottomSheetDialog.dismiss();
            }

            @Override
            public void onToggleFavorite(LeetOption leetOption) {
                toggleFavoriteFromBottomSheet(leetOption, mainAdapterHolder[0]);
                // Update auch die Favoriten-Liste
                updateFavoritesList(recyclerViewFavorites, favoritesHeader, favoritesAdapterHolder[0]);
            }

            @Override
            public void onQuickTest(LeetOption leetOption) {
                // Quick test implementation
            }

            @Override
            public void onShowTable(LeetOption leetOption) {
                showTableDialog(leetOption);
                bottomSheetDialog.dismiss();
            }
        };

        // Hauptliste erstellen
        List<LeetOption> allOptions = createLeetOptionsList();
        LeetSelectorAdapter mainAdapter = new LeetSelectorAdapter(allOptions, listener);
        mainAdapterHolder[0] = mainAdapter;
        recyclerViewLeets.setAdapter(mainAdapter);

        // Favoriten-Liste erstellen und anzeigen
        updateFavoritesList(recyclerViewFavorites, favoritesHeader, favoritesAdapterHolder[0]);

        // "Neu" Button
        buttonAddNewLeet.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showNewProfileDialog();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    // Neue Hilfsmethode für Favoriten-Update
    private void updateFavoritesList(RecyclerView recyclerViewFavorites, TextView favoritesHeader, LeetSelectorAdapter favoritesAdapter) {
        List<LeetOption> favoriteOptions = getFavoriteOptions();

        if (favoriteOptions.isEmpty()) {
            // Keine Favoriten: Header und Liste verstecken
            favoritesHeader.setVisibility(View.GONE);
            recyclerViewFavorites.setVisibility(View.GONE);
        } else {
            // Favoriten vorhanden: Header und Liste anzeigen
            favoritesHeader.setVisibility(View.VISIBLE);
            recyclerViewFavorites.setVisibility(View.VISIBLE);

            // Adapter erstellen oder updaten
            if (favoritesAdapter == null) {
                LeetSelectorAdapter newFavoritesAdapter = new LeetSelectorAdapter(favoriteOptions,
                        new LeetSelectorAdapter.OnLeetSelectedListener() {
                            @Override
                            public void onLeetSelected(LeetOption leetOption) {
                                selectLeet(leetOption);
                                // Bottom sheet wird vom Parent-Listener geschlossen
                            }
                            // Andere Methoden können leer bleiben oder delegieren
                            @Override public void onLeetPreview(LeetOption leetOption) {}
                            @Override public void onEditLeet(LeetOption leetOption) {}
                            @Override public void onToggleFavorite(LeetOption leetOption) {}
                            @Override public void onQuickTest(LeetOption leetOption) {}
                            @Override public void onShowTable(LeetOption leetOption) {}
                        });
                recyclerViewFavorites.setAdapter(newFavoritesAdapter);
            } else {
                favoritesAdapter.updateOptions(favoriteOptions);
            }
        }
    }

    // Neue Hilfsmethode um nur Favoriten zu finden
    private List<LeetOption> getFavoriteOptions() {
        List<LeetOption> favorites = new ArrayList<>();
        List<LeetOption> allOptions = createLeetOptionsList();

        for (LeetOption option : allOptions) {
            if (option.isFavorite()) {
                favorites.add(option);
            }
        }

        return favorites;
    }
}