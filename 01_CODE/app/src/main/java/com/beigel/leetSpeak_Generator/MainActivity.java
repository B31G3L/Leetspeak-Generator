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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SIMPLE = 0;
    private static final int EXTENDED = 1;
    private static final int CUSTOM = 2;
    private static final int ABOUT = 3;

    private final char[] plaintextAlphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    private final String[] simpleLeetAlphabet = {"4", "8", "C", "D", "3", "F", "6", "#", "1", "J", "K", "L", "M", "N", "0", "P", "Q", "R", "5", "7", "U", "V", "W", "X", "Y", "2"};

    private final String[] extendedLeetAlphabet = {"4", "8", "(", "|)", "3", "|=", "6", "#", "!", "_|", "|<", "1", "/\\/\\", "|\\|", "0", "9", "0_", "2", "5", "7", "|_|", "\\/", "\\/\\/", "><", "`/", "Z"};

    private int activeMode = SIMPLE;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabAddLeet;
    private EditText inputPlainText;
    private TextView outputLeetText;
    private Button buttonCopy;
    private TextView tableTitle;
    private TableLayout leetTable;
    private TextView appTitle;
    private TextView navHeaderTitle;
    private MaterialButton buttonExpandTable;
    private MaterialCardView tableContainer;
    private Toolbar toolbar;

    // Für About-Ansicht
    private View mainContentView;
    private View aboutView;

    private ProfileManager profileManager;
    private EditText[][] editableFields = new EditText[13][2];
    private TextView[][] displayFields = new TextView[13][2];
    private boolean isEditMode = false;
    private boolean isTableExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize NavigationView
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize ProfileManager
        profileManager = new ProfileManager(this);

        // Initialize UI elements
        inputPlainText = findViewById(R.id.inputPlainText);
        outputLeetText = findViewById(R.id.outputLeetText);
        buttonCopy = findViewById(R.id.buttonCopy);
        tableTitle = findViewById(R.id.tableTitle);
        leetTable = findViewById(R.id.leetTable);
        appTitle = findViewById(R.id.appTitle);
        buttonExpandTable = findViewById(R.id.buttonExpandTable);
        tableContainer = findViewById(R.id.tableContainer);
        fabAddLeet = findViewById(R.id.fabAddLeet);

        // Initialisiere Views für den Hauptinhalt und About-Ansicht
        mainContentView = findViewById(R.id.main_content);
        // About-View wird dynamisch geladen, wenn benötigt

        // Get the navigation header view
        View headerView = navigationView.getHeaderView(0);
        navHeaderTitle = headerView.findViewById(R.id.navHeaderTitle);

        // Set up text change listener
        inputPlainText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateOutput();
            }
        });

        // Set up button click listeners
        buttonCopy.setOnClickListener(v -> copyToClipboard());

        buttonExpandTable.setOnClickListener(v -> toggleTableVisibility());

        // Neuer FAB-ClickListener
        fabAddLeet.setOnClickListener(v -> showNewProfileDialog());

        // Set initial mode and update UI
        setActiveMode(SIMPLE);
        updateNavigationView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Toolbar-Menü aufblasen
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Aktualisiere Sichtbarkeit der Menüpunkte basierend auf dem aktiven Modus
        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem saveItem = menu.findItem(R.id.action_save);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);

        if (editItem != null && saveItem != null && deleteItem != null) {
            boolean isCustomMode = activeMode == CUSTOM;
            boolean isCustomAndNotDefault = isCustomMode && profileManager.getCurrentProfileIndex() > 0;

            editItem.setVisible(isCustomMode && !isEditMode);
            saveItem.setVisible(isCustomMode && isEditMode);
            deleteItem.setVisible(isCustomAndNotDefault);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_edit) {
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

    private void toggleTableVisibility() {
        isTableExpanded = !isTableExpanded;
        tableContainer.setVisibility(isTableExpanded ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_simple) {
            setActiveMode(SIMPLE);
        } else if (itemId == R.id.nav_extended) {
            setActiveMode(EXTENDED);
        } else if (itemId == R.id.nav_custom_default) {
            setActiveMode(CUSTOM);
            profileManager.setCurrentProfileIndex(0); // Standard-Leet auswählen
            updateTableTitle();
            updateOutput();
        } else if (itemId == R.id.nav_about) {
            showAboutView();
            item.setChecked(true);
        } else if (itemId >= 100) {
            // Benutzerdefinierte Leets haben IDs ≥ 100
            int leetIndex = itemId - 100;

            // Debug-Logging
            Log.d("MainActivity", "Ausgewählter benutzerdefinierter Leet ID: " + itemId + ", Index: " + leetIndex);

            if (leetIndex > 0 && leetIndex < profileManager.getProfiles().size()) {
                try {
                    setActiveMode(CUSTOM);
                    profileManager.setCurrentProfileIndex(leetIndex);
                    updateTableTitle();
                    updateOutput();
                    updateTable();

                    // Debug-Message für den Benutzer
                    Toast.makeText(this, "Leet '" + profileManager.getCurrentProfile().getName() +
                            "' ausgewählt", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("MainActivity", "Fehler beim Auswählen des Leets: " + e.getMessage());
                    Toast.makeText(this, "Fehler beim Auswählen des Leets: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Neue Methode zum Anzeigen der About-Ansicht
    private void showAboutView() {
        // Wenn wir bereits die About-Ansicht anzeigen, nichts tun
        if (activeMode == ABOUT) return;

        // About-View laden, falls es noch nicht existiert
        if (aboutView == null) {
            aboutView = getLayoutInflater().inflate(R.layout.layout_about, null, false);

            // About-View dem Layout hinzufügen mit gleichen Layout-Parametern wie mainContentView
            DrawerLayout.LayoutParams layoutParams = new DrawerLayout.LayoutParams(
                    DrawerLayout.LayoutParams.MATCH_PARENT,
                    DrawerLayout.LayoutParams.MATCH_PARENT
            );
            drawerLayout.addView(aboutView, layoutParams);
        }

        // Hauptcontent verstecken und About-View anzeigen
        mainContentView.setVisibility(View.GONE);
        aboutView.setVisibility(View.VISIBLE);

        // FAB ausblenden
        fabAddLeet.hide();

        // App-Titel ändern
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.about);
        }

        activeMode = ABOUT;

        // Toolbar-Menü aktualisieren
        invalidateOptionsMenu();
    }

    // Angepasste Methode, um zurück zur Hauptansicht zu wechseln
    private void showMainView() {
        if (aboutView != null) {
            aboutView.setVisibility(View.GONE);
        }
        mainContentView.setVisibility(View.VISIBLE);

        // FAB wieder anzeigen
        fabAddLeet.show();

        // App-Titel zurücksetzen
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (activeMode == ABOUT) {
            // Zurück zur Hauptansicht, wenn wir in der About-Ansicht sind
            setActiveMode(SIMPLE);
        } else {
            super.onBackPressed();
        }
    }

    private void updateNavigationView() {
        // Update visibility of save/delete options
        // Update checkmarks for current mode
        MenuItem simpleItem = navigationView.getMenu().findItem(R.id.nav_simple);
        MenuItem extendedItem = navigationView.getMenu().findItem(R.id.nav_extended);
        MenuItem customDefaultItem = navigationView.getMenu().findItem(R.id.nav_custom_default);
        MenuItem aboutItem = navigationView.getMenu().findItem(R.id.nav_about);

        if (simpleItem != null) {
            simpleItem.setChecked(activeMode == SIMPLE);
        }

        if (extendedItem != null) {
            extendedItem.setChecked(activeMode == EXTENDED);
        }

        if (customDefaultItem != null) {
            customDefaultItem.setChecked(activeMode == CUSTOM && profileManager.getCurrentProfileIndex() == 0);
        }

        if (aboutItem != null) {
            aboutItem.setChecked(activeMode == ABOUT);
        }

        // Update custom profiles in menu
        updateCustomProfilesInMenu();

        // Aktualisiere Toolbar-Menü
        invalidateOptionsMenu();
    }

    private void updateCustomProfilesInMenu() {
        try {
            // Hole die benutzerdefinierten Leets
            List<CustomProfile> leets = profileManager.getProfiles();

            // Navigationsansicht finden
            NavigationView navigationView = findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();

            // Finde das "Custom Leets" Menü-Item
            MenuItem customLeetsItem = null;
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getTitle().equals(getString(R.string.custom_profiles))) {
                    customLeetsItem = item;
                    break;
                }
            }

            if (customLeetsItem != null && customLeetsItem.hasSubMenu()) {
                // Das Untermenü für benutzerdefinierte Leets
                SubMenu customSubMenu = customLeetsItem.getSubMenu();

                // Entferne alle alten benutzerdefinierten Leets, aber behalte das Standard-Leet (index 0)
                // Wir gehen rückwärts durch, um Probleme mit den Indizes zu vermeiden
                for (int i = customSubMenu.size() - 1; i > 0; i--) {
                    MenuItem item = customSubMenu.getItem(i);
                    if (item.getItemId() != R.id.nav_custom_default) {
                        customSubMenu.removeItem(item.getItemId());
                    }
                }

                // Füge benutzerdefinierte Leets hinzu
                for (int i = 1; i < leets.size(); i++) {  // Start bei 1, um das Standard-Leet zu überspringen
                    CustomProfile leet = leets.get(i);
                    MenuItem item = customSubMenu.add(Menu.NONE, 100 + i, Menu.NONE, leet.getName());
                    item.setIcon(R.drawable.ic_custom_mode);
                    item.setCheckable(true);
                    item.setChecked(activeMode == CUSTOM && profileManager.getCurrentProfileIndex() == i);
                }
            }
        } catch (Exception e) {
            // Falls etwas schiefgeht, loggen wir den Fehler
            Log.e("MainActivity", "Fehler beim Aktualisieren der benutzerdefinierten Leets: " + e.getMessage());
            // Toast anzeigen, um den Benutzer zu informieren
            Toast.makeText(this, "Fehler beim Aktualisieren des Menüs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showNewProfileDialog() {
        // Verwende den Standard-Theme anstelle des benutzerdefinierten Themes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.create_new_profile);

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint(R.string.enter_profile_name);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String leetName = input.getText().toString().trim();
                if (leetName.isEmpty()) {
                    leetName = getString(R.string.default_custom_name);
                }

                // Erstelle neuen Leet mit aktuellen Buchstaben als Standard
                CustomProfile newLeet = new CustomProfile(leetName);

                // Initialisiere mit aktuellen Buchstaben vom aktiven Modus
                for (char c : plaintextAlphabet) {
                    String plainChar = String.valueOf(c);
                    String leetChar = getTranslatedChar(c);
                    newLeet.setTranslation(plainChar, leetChar);
                }

                try {
                    // Leet hinzufügen
                    profileManager.addProfile(newLeet);

                    // UI aktualisieren
                    setActiveMode(CUSTOM);
                    profileManager.setCurrentProfileIndex(profileManager.getProfiles().size() - 1);
                    updateTableTitle();
                    updateTable();

                    // Wichtig: Menü aktualisieren, um den neuen Leet anzuzeigen
                    updateNavigationView();

                    // Erfolgsmeldung
                    Toast.makeText(MainActivity.this, "Leet '" + leetName + "' erstellt und ausgewählt",
                            Toast.LENGTH_SHORT).show();

                    // Debug-Log
                    Log.d("MainActivity", "Neuer Leet erstellt: " + leetName + ", Index: " +
                            (profileManager.getProfiles().size() - 1));
                } catch (Exception e) {
                    Log.e("MainActivity", "Fehler beim Erstellen des Leets: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Fehler beim Erstellen des Leets: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showDeleteProfileConfirmDialog() {
        // Verwende den Standard-Theme anstelle des benutzerdefinierten Themes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_confirm)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        profileManager.deleteCurrentProfile();
                        Toast.makeText(MainActivity.this, R.string.profile_deleted, Toast.LENGTH_SHORT).show();

                        // Nach dem Löschen zum Simple Leet wechseln
                        setActiveMode(SIMPLE);

                        // UI aktualisieren
                        updateNavigationView();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void saveCurrentCustomProfile() {
        if (isEditMode) {
            saveCustomTranslations();
        }
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
    }

    private void setActiveMode(int mode) {
        activeMode = mode;

        // Zeige FAB nur in normalen Modi, nicht im About Modus
        if (mode != ABOUT) {
            showMainView();
            fabAddLeet.show();
        } else {
            fabAddLeet.hide();
        }

        // Update app title based on mode
        switch (mode) {
            case SIMPLE:
                appTitle.setText(R.string.simple);
                if (navHeaderTitle != null) {
                    navHeaderTitle.setText(R.string.simple);
                }
                tableTitle.setText(R.string.simple_table_title);
                break;
            case EXTENDED:
                appTitle.setText(R.string.extended);
                if (navHeaderTitle != null) {
                    navHeaderTitle.setText(R.string.extended);
                }
                tableTitle.setText(R.string.extended_table_title);
                break;
            case CUSTOM:
                appTitle.setText(R.string.custom);
                if (navHeaderTitle != null) {
                    navHeaderTitle.setText(R.string.custom);
                }
                updateTableTitle();
                break;
            case ABOUT:
                // Navigiere zur About-Ansicht
                showAboutView();
                break;
        }

        // Exit edit mode if changing modes
        if (isEditMode) {
            isEditMode = false;
        }

        // Update placeholder
        String placeholder = getString(R.string.plaintext_placeholder);
        inputPlainText.setHint(placeholder);

        // Update navigation view
        updateNavigationView();

        // Update output and table
        updateOutput();
        updateTable();

        // Update Toolbar actions
        invalidateOptionsMenu();
    }

    private void updateTableTitle() {
        if (activeMode == CUSTOM) {
            CustomProfile profile = profileManager.getCurrentProfile();
            tableTitle.setText(profile.getName() + " " + getString(R.string.custom_table_title));
        }
    }

    private void updateOutput() {
        String input = inputPlainText.getText().toString();
        String output = translate(input);
        outputLeetText.setText(output);
    }

    private String translate(String input) {
        StringBuilder output = new StringBuilder();

        for (char c : input.toCharArray()) {
            String outputChar = getTranslatedChar(c);
            output.append(outputChar);
        }

        return output.toString();
    }

    private String getTranslatedChar(char inputChar) {
        // Convert to uppercase for lookup
        char upperChar = Character.toUpperCase(inputChar);

        // Find the index in the alphabet
        int index = -1;
        for (int i = 0; i < plaintextAlphabet.length; i++) {
            if (plaintextAlphabet[i] == upperChar) {
                index = i;
                break;
            }
        }

        // If not found in alphabet, return original character
        if (index == -1) {
            return String.valueOf(inputChar);
        }

        // Translate based on active mode
        switch (activeMode) {
            case SIMPLE:
                return simpleLeetAlphabet[index];
            case EXTENDED:
                return extendedLeetAlphabet[index];
            case CUSTOM:
                return profileManager.getCurrentProfile().getTranslation(String.valueOf(upperChar));
            default:
                return String.valueOf(inputChar);
        }
    }

    private void updateTable() {
        // Clear the table
        leetTable.removeAllViews();

        // Tabellenzellen-Stil
        int cellPadding = (int) getResources().getDimension(R.dimen.table_cell_padding);

        // Add rows to the table
        for (int i = 0; i < 13; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ?
                    getResources().getColor(R.color.gray_light, getTheme()) :
                    getResources().getColor(android.R.color.transparent, getTheme()));

            // Left side (letters 0-12)
            TextView leftPlain = new TextView(this);
            leftPlain.setText(String.valueOf(plaintextAlphabet[i]));
            leftPlain.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
            leftPlain.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
            leftPlain.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
            leftPlain.setGravity(Gravity.CENTER);
            row.addView(leftPlain);

            // Left side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText leftLeet = new EditText(this);
                leftLeet.setText(getTranslatedChar(plaintextAlphabet[i]));
                leftLeet.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                leftLeet.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
                leftLeet.setGravity(Gravity.CENTER);
                leftLeet.setBackgroundResource(R.drawable.edit_text_background);
                leftLeet.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
                editableFields[i][0] = leftLeet;
                row.addView(leftLeet);
            } else {
                TextView leftLeet = new TextView(this);
                leftLeet.setText(getTranslatedChar(plaintextAlphabet[i]));
                leftLeet.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
                leftLeet.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                leftLeet.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
                leftLeet.setGravity(Gravity.CENTER);
                if (!isEditMode) {
                    displayFields[i][0] = leftLeet;
                }
                row.addView(leftLeet);
            }

            // Right side (letters 13-25)
            TextView rightPlain = new TextView(this);
            rightPlain.setText(String.valueOf(plaintextAlphabet[i + 13]));
            rightPlain.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
            rightPlain.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
            rightPlain.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
            rightPlain.setGravity(Gravity.CENTER);
            row.addView(rightPlain);

            // Right side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText rightLeet = new EditText(this);
                rightLeet.setText(getTranslatedChar(plaintextAlphabet[i + 13]));
                rightLeet.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                rightLeet.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
                rightLeet.setGravity(Gravity.CENTER);
                rightLeet.setBackgroundResource(R.drawable.edit_text_background);
                rightLeet.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
                editableFields[i][1] = rightLeet;
                row.addView(rightLeet);
            } else {
                TextView rightLeet = new TextView(this);
                rightLeet.setText(getTranslatedChar(plaintextAlphabet[i + 13]));
                rightLeet.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
                rightLeet.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                rightLeet.setTypeface(ResourcesCompat.getFont(this, R.font.raleway));
                rightLeet.setGravity(Gravity.CENTER);
                if (!isEditMode) {
                    displayFields[i][1] = rightLeet;
                }
                row.addView(rightLeet);
            }

            leetTable.addView(row);
        }
    }

    private void switchToEditMode() {
        if (activeMode != CUSTOM) return;

        isEditMode = true;
        updateTable();

        // Toolbar-Menü aktualisieren
        invalidateOptionsMenu();
    }

    private void saveCustomTranslations() {
        if (!isEditMode) return;

        CustomProfile currentProfile = profileManager.getCurrentProfile();
        Map<String, String> translations = new HashMap<>();

        // Save left column (letters 0-12)
        for (int i = 0; i < 13; i++) {
            String plainChar = String.valueOf(plaintextAlphabet[i]);
            String leetChar = editableFields[i][0].getText().toString();
            translations.put(plainChar, leetChar);
        }

        // Save right column (letters 13-25)
        for (int i = 0; i < 13; i++) {
            String plainChar = String.valueOf(plaintextAlphabet[i + 13]);
            String leetChar = editableFields[i][1].getText().toString();
            translations.put(plainChar, leetChar);
        }

        currentProfile.setTranslations(translations);
        profileManager.updateCurrentProfile(currentProfile);

        isEditMode = false;

        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();

        updateTable();
        updateOutput();

        // Toolbar-Menü aktualisieren
        invalidateOptionsMenu();
    }

    private void copyToClipboard() {
        String text = outputLeetText.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Leetspeak Text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show();
    }
}