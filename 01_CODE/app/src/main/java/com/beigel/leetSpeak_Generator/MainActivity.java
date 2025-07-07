package com.beigel.leetSpeak_Generator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.util.TypedValue;
import androidx.core.content.res.ResourcesCompat;
import android.view.inputmethod.InputMethodManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.beigel.leetSpeak_Generator.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SIMPLE = ProfileManager.MODE_SIMPLE;
    private static final int EXTENDED = ProfileManager.MODE_EXTENDED;
    private static final int CUSTOM = ProfileManager.MODE_CUSTOM;
    private static final int ABOUT = ProfileManager.MODE_ABOUT;

    private int activeMode = SIMPLE;

    // ViewBinding - nur für ActivityMain
    private ActivityMainBinding binding;

    // Navigation Header Views (manuell referenziert)
    private TextView navHeaderTitle;

    // Repository Pattern
    private ProfileRepository profileRepository;

    // Für About-Ansicht
    private View aboutView;

    private EditText[][] editableFields = new EditText[13][2];
    private TextView[][] displayFields = new TextView[13][2];
    private boolean isEditMode = false;
    private boolean isTableExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding Setup
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Navigation Header manuell referenzieren
        View headerView = binding.navView.getHeaderView(0);
        navHeaderTitle = headerView.findViewById(R.id.navHeaderTitle);

        // Repository initialisieren
        profileRepository = new ProfileRepository(this);

        // UI Setup
        initializeViews();
        setupNavigation();

        // Favoriten laden - jetzt asynchron!
        loadFavoriteMode();

        // App-Name im Navigation Header setzen
        if (navHeaderTitle != null) {
            navHeaderTitle.setText(R.string.app_name);
        }
    }

    private void initializeViews() {
        // Toolbar Setup
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // Event Listeners mit ViewBinding
        binding.inputPlainText.addTextChangedListener(textWatcher);
        binding.buttonCopy.setOnClickListener(v -> copyToClipboard());
        binding.buttonExpandTable.setOnClickListener(v -> toggleTableVisibility());
        binding.fabAddLeet.setOnClickListener(v -> showNewProfileDialog());
    }

    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar,
                R.string.open_drawer, R.string.close_drawer);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);
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
                updateTable();
                updateNavigationView();
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
                        updateNavigationView();

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

    private void toggleTableVisibility() {
        isTableExpanded = !isTableExpanded;

        // Button text und Animation
        binding.buttonExpandTable.setText(isTableExpanded ?
                R.string.table_hide : R.string.table_expand);

        AnimationHelper.scaleButton(binding.buttonExpandTable);

        if (isTableExpanded) {
            AnimationHelper.expandCard(binding.tableContainer, () -> {
                AnimationHelper.staggeredFadeIn(binding.leetTable, 50);
            });
        } else {
            AnimationHelper.collapseCard(binding.tableContainer, null);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_simple) {
            setActiveMode(SIMPLE);
        } else if (itemId == R.id.nav_extended) {
            setActiveMode(EXTENDED);
        } else if (itemId == R.id.nav_about) {
            showAboutView();
            item.setChecked(true);
        } else if (itemId >= 100) {
            int leetIndex = itemId - 100;

            if (leetIndex >= 0 && leetIndex < profileRepository.getProfiles().size()) {
                try {
                    setActiveMode(CUSTOM);
                    profileRepository.setCurrentProfileIndex(leetIndex);

                    updateOutput();
                    updateTable();
                    updateNavigationView();

                    String profileName = profileRepository.getCurrentProfile().getName();
                    binding.appTitle.setText(profileName + " Leet");

                    Toast.makeText(this, "Leet '" + profileRepository.getCurrentProfile().getName() +
                            "' ausgewählt", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    ErrorHandler.handleError(this, e, "Fehler beim Auswählen des Leets",
                            ErrorHandler.ErrorSeverity.ERROR);
                }
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAboutView() {
        if (activeMode == ABOUT) return;

        if (aboutView == null) {
            aboutView = getLayoutInflater().inflate(R.layout.layout_about, null, false);

            DrawerLayout.LayoutParams layoutParams = new DrawerLayout.LayoutParams(
                    DrawerLayout.LayoutParams.MATCH_PARENT,
                    DrawerLayout.LayoutParams.MATCH_PARENT
            );
            binding.drawerLayout.addView(aboutView, layoutParams);
        }

        // Smooth Crossfade zwischen Main und About
        AnimationHelper.crossFade(binding.mainContent, aboutView, 400, () -> {
            binding.fabAddLeet.hide();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.about);
            }

            activeMode = ABOUT;
            invalidateOptionsMenu();
        });
    }

    private void showMainView() {
        if (aboutView != null && aboutView.getVisibility() == View.VISIBLE) {
            AnimationHelper.crossFade(aboutView, binding.mainContent, 400, () -> {
                binding.fabAddLeet.show();

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("");
                }
            });
        } else {
            if (aboutView != null) {
                aboutView.setVisibility(View.GONE);
            }
            binding.mainContent.setVisibility(View.VISIBLE);
            binding.fabAddLeet.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (activeMode == ABOUT) {
            setActiveMode(SIMPLE);
        } else {
            super.onBackPressed();
        }
    }

    private void updateNavigationView() {
        MenuItem simpleItem = binding.navView.getMenu().findItem(R.id.nav_simple);
        MenuItem extendedItem = binding.navView.getMenu().findItem(R.id.nav_extended);
        MenuItem aboutItem = binding.navView.getMenu().findItem(R.id.nav_about);

        if (simpleItem != null) {
            simpleItem.setChecked(activeMode == SIMPLE);
            boolean isSimpleFavorite = profileRepository.getProfileManager().isFavorite(SIMPLE, 0);
            simpleItem.setIcon(isSimpleFavorite ?
                    R.drawable.ic_simple_mode_favorite :
                    R.drawable.ic_simple_mode);
        }

        if (extendedItem != null) {
            extendedItem.setChecked(activeMode == EXTENDED);
            boolean isExtendedFavorite = profileRepository.getProfileManager().isFavorite(EXTENDED, 0);
            extendedItem.setIcon(isExtendedFavorite ?
                    R.drawable.ic_extended_mode_favorite :
                    R.drawable.ic_extended_mode);
        }

        if (aboutItem != null) {
            aboutItem.setChecked(activeMode == ABOUT);
        }

        updateCustomProfilesInMenu();
        invalidateOptionsMenu();
    }

    private void updateCustomProfilesInMenu() {
        try {
            List<CustomProfile> leets = profileRepository.getProfiles();
            NavigationView navigationView = binding.navView;
            Menu menu = navigationView.getMenu();

            MenuItem customLeetsItem = null;
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getTitle().equals(getString(R.string.custom_profiles))) {
                    customLeetsItem = item;
                    break;
                }
            }

            if (customLeetsItem != null && customLeetsItem.hasSubMenu()) {
                SubMenu customSubMenu = customLeetsItem.getSubMenu();
                customSubMenu.clear();

                if (leets.isEmpty()) {
                    MenuItem noProfilesItem = customSubMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.no_custom_profiles);
                    noProfilesItem.setIcon(R.drawable.ic_custom_mode);
                    noProfilesItem.setEnabled(false);
                } else {
                    for (int i = 0; i < leets.size(); i++) {
                        CustomProfile leet = leets.get(i);
                        MenuItem item = customSubMenu.add(Menu.NONE, 100 + i, Menu.NONE, leet.getName());

                        int iconResId = leet.getIconResId();
                        if (iconResId != 0) {
                            if (profileRepository.getProfileManager().isFavorite(CUSTOM, i)) {
                                Drawable originalIcon = getResources().getDrawable(iconResId, getTheme());
                                Drawable starIcon = getResources().getDrawable(R.drawable.ic_favorite_star, getTheme());

                                Drawable[] layers = new Drawable[2];
                                layers[0] = originalIcon;
                                layers[1] = starIcon;

                                LayerDrawable layerDrawable = new LayerDrawable(layers);
                                int starSize = (int) getResources().getDimension(R.dimen.star_indicator_size);
                                layerDrawable.setLayerInset(1, originalIcon.getIntrinsicWidth() - starSize,
                                        0, 0, originalIcon.getIntrinsicHeight() - starSize);

                                item.setIcon(layerDrawable);
                            } else {
                                item.setIcon(iconResId);
                            }
                        } else {
                            if (profileRepository.getProfileManager().isFavorite(CUSTOM, i)) {
                                item.setIcon(R.drawable.ic_custom_mode_favorite);
                            } else {
                                item.setIcon(R.drawable.ic_custom_mode);
                            }
                        }

                        item.setCheckable(true);
                        item.setChecked(activeMode == CUSTOM && profileRepository.getCurrentProfileIndex() == i);
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.handleError(this, e, "Fehler beim Aktualisieren des Menüs",
                    ErrorHandler.ErrorSeverity.WARNING);
        }
    }

    private void showNewProfileDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_profile, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText editTextProfileName = dialogView.findViewById(R.id.editTextProfileName);
        ImageView selectedIcon = dialogView.findViewById(R.id.selectedIcon);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonCreate = dialogView.findViewById(R.id.buttonCreate);

        final int[] selectedIconResId = {R.drawable.ic_custom_mode};

        selectedIcon.setOnClickListener(v -> {
            AnimationHelper.scaleButton(v);
            IconSelectorDialog iconDialog = new IconSelectorDialog(
                    MainActivity.this,
                    (iconResId) -> {
                        selectedIconResId[0] = iconResId;
                        selectedIcon.setImageResource(iconResId);
                        AnimationHelper.pulse(selectedIcon, 1);
                    },
                    selectedIconResId[0]
            );
            iconDialog.show();
        });

        buttonCancel.setOnClickListener(v -> {
            AnimationHelper.scaleButton(v);
            dialog.dismiss();
        });

        buttonCreate.setOnClickListener(v -> {
            AnimationHelper.scaleButton(v);

            String leetName = editTextProfileName.getText().toString().trim();
            if (leetName.isEmpty()) {
                leetName = getString(R.string.default_custom_name);
            }

            createNewProfileWithRepository(leetName, selectedIconResId[0]);
            dialog.dismiss();
        });

        dialog.show();

        editTextProfileName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextProfileName, InputMethodManager.SHOW_IMPLICIT);
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

                    updateTable();
                    updateNavigationView();
                    updateAppTitle();

                    AnimationHelper.pulse(binding.fabAddLeet, 2);

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

                        updateTable();
                        updateOutput();
                    }

                    updateNavigationView();
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

        if (mode != ABOUT) {
            showMainView();
            binding.fabAddLeet.show();
        } else {
            binding.fabAddLeet.hide();
        }

        updateAppTitle();
        updateNavigationView();
        updateOutput();
        updateTable();
        invalidateOptionsMenu();
    }

    private void updateAppTitle() {
        String title;
        switch (activeMode) {
            case SIMPLE:
                title = getString(R.string.simple);
                break;
            case EXTENDED:
                title = getString(R.string.extended);
                break;
            case CUSTOM:
                CustomProfile currentProfile = profileRepository.getCurrentProfile();
                if (currentProfile != null) {
                    title = currentProfile.getName() + " Leet";
                } else {
                    title = getString(R.string.custom);
                }
                break;
            default:
                title = getString(R.string.app_name);
        }
        binding.appTitle.setText(title);
    }

    private void updateOutput() {
        String input = binding.inputPlainText.getText().toString();

        LeetTranslator.TranslationMode mode = getCurrentTranslationMode();
        CustomProfile currentProfile = (mode == LeetTranslator.TranslationMode.CUSTOM)
                ? profileRepository.getCurrentProfile()
                : null;

        String output = LeetTranslator.translate(input, mode, currentProfile);
        binding.outputLeetText.setText(output);
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

    private void updateTable() {
        binding.leetTable.removeAllViews();

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

            // Left side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText leftLeet = createTableEditText(getTranslatedChar(alphabet[i]), cellPadding);
                editableFields[i][0] = leftLeet;
                row.addView(leftLeet);
            } else {
                TextView leftLeet = createTableTextView(getTranslatedChar(alphabet[i]), cellPadding);
                if (!isEditMode) {
                    displayFields[i][0] = leftLeet;
                }
                row.addView(leftLeet);
            }

            // Right side (letters 13-25)
            TextView rightPlain = createTableTextView(String.valueOf(alphabet[i + 13]), cellPadding);
            row.addView(rightPlain);

            // Right side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText rightLeet = createTableEditText(getTranslatedChar(alphabet[i + 13]), cellPadding);
                editableFields[i][1] = rightLeet;
                row.addView(rightLeet);
            } else {
                TextView rightLeet = createTableTextView(getTranslatedChar(alphabet[i + 13]), cellPadding);
                if (!isEditMode) {
                    displayFields[i][1] = rightLeet;
                }
                row.addView(rightLeet);
            }

            binding.leetTable.addView(row);
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

        showComprehensiveEditDialog();
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
                            updateTable();
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

    private void copyToClipboard() {
        String text = binding.outputLeetText.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Leetspeak Text", text);
        clipboard.setPrimaryClip(clip);

        ErrorHandler.showSnackbar(this, getString(R.string.copy_success), Snackbar.LENGTH_SHORT);
    }

    private void showComprehensiveEditDialog() {
        CustomProfile currentProfile = profileRepository.getCurrentProfile();

        if (currentProfile == null) {
            ErrorHandler.showSnackbar(this, getString(R.string.no_custom_profiles_available),
                    Snackbar.LENGTH_SHORT);
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comprehensive_edit, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText editTextProfileName = dialogView.findViewById(R.id.editTextProfileName);
        ImageView selectedIcon = dialogView.findViewById(R.id.selectedIcon);
        TableLayout editTable = dialogView.findViewById(R.id.editTable);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        editTextProfileName.setText(currentProfile.getName());

        int iconResId = currentProfile.getIconResId();
        if (iconResId != 0) {
            selectedIcon.setImageResource(iconResId);
        }

        final int[] selectedIconResId = {iconResId};

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

        EditText[][] tableEditFields = new EditText[13][2];

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

            TextView leftPlain = createTableTextView(String.valueOf(alphabet[i]), cellPadding);
            row.addView(leftPlain);

            EditText leftLeet = createTableEditText(currentProfile.getTranslation(String.valueOf(alphabet[i])), cellPadding);
            tableEditFields[i][0] = leftLeet;
            row.addView(leftLeet);

            TextView rightPlain = createTableTextView(String.valueOf(alphabet[i + 13]), cellPadding);
            row.addView(rightPlain);

            EditText rightLeet = createTableEditText(currentProfile.getTranslation(String.valueOf(alphabet[i + 13])), cellPadding);
            tableEditFields[i][1] = rightLeet;
            row.addView(rightLeet);

            editTable.addView(row);
        }

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSave.setOnClickListener(v -> {
            String profileName = editTextProfileName.getText().toString().trim();
            if (profileName.isEmpty()) {
                profileName = getString(R.string.default_custom_name);
            }

            try {
                currentProfile.setName(profileName);
                currentProfile.setIconResId(selectedIconResId[0]);

                for (int i = 0; i < 13; i++) {
                    String plainChar = String.valueOf(alphabet[i]);
                    String leetChar = tableEditFields[i][0].getText().toString();
                    currentProfile.setTranslation(plainChar, leetChar);

                    plainChar = String.valueOf(alphabet[i + 13]);
                    leetChar = tableEditFields[i][1].getText().toString();
                    currentProfile.setTranslation(plainChar, leetChar);
                }

                String finalProfileName = profileName;
                profileRepository.updateProfile(profileRepository.getCurrentProfileIndex(), currentProfile,
                        new ProfileRepository.ProfileOperationCallback() {
                            @Override
                            public void onComplete(ProfileOperationResult result) {
                                if (result.isSuccess()) {
                                    binding.appTitle.setText(finalProfileName + " Leet");
                                    updateNavigationView();
                                    updateOutput();
                                    updateTable();

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
        });

        dialog.show();

        editTextProfileName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextProfileName, InputMethodManager.SHOW_IMPLICIT);
    }

    // TextWatcher für Live-Updates
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            updateOutput();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cleanup
        if (profileRepository != null) {
            profileRepository.cleanup();
        }

        // ViewBinding cleanup
        binding = null;
        navHeaderTitle = null;
    }
}
