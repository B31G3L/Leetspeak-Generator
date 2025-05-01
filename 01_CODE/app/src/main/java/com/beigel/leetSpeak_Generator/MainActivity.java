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
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SIMPLE = ProfileManager.MODE_SIMPLE;
    private static final int EXTENDED = ProfileManager.MODE_EXTENDED;
    private static final int CUSTOM = ProfileManager.MODE_CUSTOM;
    private static final int ABOUT = ProfileManager.MODE_ABOUT;

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

        // Zuerst alle View-Elemente initialisieren
        initializeViews();

        // Dann die Navigation und den ProfileManager konfigurieren
        setupNavigation();

        // ProfileManager initialisieren
        profileManager = new ProfileManager(this);

        // Zuletzt den Favoriten laden und aktivieren
        loadFavoriteMode();

        // Setze den App-Namen im Navigation Header
        if (navHeaderTitle != null) {
            navHeaderTitle.setText(R.string.app_name);
        }
    }

    // View-Initialisierung in eine separate Methode auslagern
    private void initializeViews() {
        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // UI-Elemente finden
        inputPlainText = findViewById(R.id.inputPlainText);
        outputLeetText = findViewById(R.id.outputLeetText);
        buttonCopy = findViewById(R.id.buttonCopy);
        leetTable = findViewById(R.id.leetTable);
        appTitle = findViewById(R.id.appTitle);
        buttonExpandTable = findViewById(R.id.buttonExpandTable);
        tableContainer = findViewById(R.id.tableContainer);
        fabAddLeet = findViewById(R.id.fabAddLeet);

        // Initialisiere Views für den Hauptinhalt und About-Ansicht
        mainContentView = findViewById(R.id.main_content);
        // About-View wird dynamisch geladen, wenn benötigt

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
        fabAddLeet.setOnClickListener(v -> showNewProfileDialog());
    }

    private void setupNavigation() {
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

        // Get the navigation header view
        View headerView = navigationView.getHeaderView(0);
        navHeaderTitle = headerView.findViewById(R.id.navHeaderTitle);
    }

    private void loadFavoriteMode() {
        int favoriteMode = profileManager.getFavoriteMode();
        int favoriteCustomIndex = profileManager.getFavoriteCustomIndex();

        // Debug-Logging
        Log.d("MainActivity", "loadFavoriteMode: FavoriteMode: " + favoriteMode +
                ", favoriteCustomIndex: " + favoriteCustomIndex);

        if (favoriteMode >= 0) {
            // Wenn ein Favorit gesetzt ist, diesen aktivieren
            if (favoriteMode == ProfileManager.MODE_SIMPLE) {
                Log.d("MainActivity", "Favorit ist Simple-Modus");
                setActiveMode(SIMPLE);
            } else if (favoriteMode == ProfileManager.MODE_EXTENDED) {
                Log.d("MainActivity", "Favorit ist Extended-Modus");
                setActiveMode(EXTENDED);
            } else if (favoriteMode == ProfileManager.MODE_CUSTOM && favoriteCustomIndex >= 0) {
                // Prüfe, ob Custom-Profile überhaupt existieren
                if (profileManager.hasProfiles() && favoriteCustomIndex < profileManager.getProfiles().size()) {
                    Log.d("MainActivity", "Favorit ist Custom-Modus mit Index: " + favoriteCustomIndex);
                    setActiveMode(CUSTOM);
                    profileManager.setCurrentProfileIndex(favoriteCustomIndex);
                } else {
                    // Falls keine Custom-Profile existieren oder der Index ungültig ist
                    Log.w("MainActivity", "Kein gültiges Custom-Profil verfügbar, setze auf Simple-Modus");
                    setActiveMode(SIMPLE);
                }
            }
        } else {
            // Standard: Simple-Modus
            Log.d("MainActivity", "Kein Favorit gesetzt, verwende Simple-Modus");
            setActiveMode(SIMPLE);
        }
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
            boolean hasCustomProfile = isCustomMode && profileManager.getCurrentProfile() != null;

            // Bearbeiten und Löschen nur anzeigen, wenn wir im Custom-Modus sind und ein Profil existiert
            editItem.setVisible(hasCustomProfile);
            deleteItem.setVisible(hasCustomProfile);

            // "Speichern"-Button ist nicht mehr erforderlich, da im Dialog gespeichert wird
            saveItem.setVisible(false);
        }

        // Aktualisiere Favoriten-Icon
        MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        if (favoriteItem != null) {
            // Favoriten-Button nur anzeigen, wenn wir nicht im About-Modus sind
            favoriteItem.setVisible(activeMode != ABOUT);

            // Nur wenn wir im Custom-Modus sind, überprüfen, ob ein Profil existiert
            if (activeMode == CUSTOM && profileManager.getCurrentProfile() == null) {
                favoriteItem.setVisible(false);
            } else {
                int customIndex = activeMode == CUSTOM ? profileManager.getCurrentProfileIndex() : 0;
                boolean isFavorite = profileManager.isFavorite(activeMode, customIndex);

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

        // Für den Custom-Modus den aktuellen Profil-Index verwenden
        if (activeMode == CUSTOM) {
            customIndex = profileManager.getCurrentProfileIndex();
        }

        // Debug-Logging vor dem Umschalten
        Log.d("MainActivity", "toggleFavorite: Aktueller Modus: " + activeMode +
                ", customIndex: " + customIndex +
                ", Aktueller Favorit: " + profileManager.getFavoriteLeetIndex());

        // Favoriten-Status umschalten
        profileManager.toggleFavorite(activeMode, customIndex);

        // Debug-Logging nach dem Umschalten
        Log.d("MainActivity", "toggleFavorite: Neuer Favorit nach dem Umschalten: " +
                profileManager.getFavoriteLeetIndex());

        // Menü aktualisieren
        invalidateOptionsMenu();

        // Navigationsmenu aktualisieren
        updateNavigationView();

        // Feedback für den Benutzer
        boolean isFavorite = profileManager.isFavorite(activeMode, customIndex);

        try {
            String name;
            if (activeMode == SIMPLE) {
                name = getString(R.string.simple);
            } else if (activeMode == EXTENDED) {
                name = getString(R.string.extended);
            } else { // CUSTOM
                name = profileManager.getCurrentProfile().getName();
            }

            int messageResId = isFavorite
                    ? R.string.favorite_added
                    : R.string.favorite_removed;

            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                Snackbar.make(
                        rootView,
                        getString(messageResId, name),
                        Snackbar.LENGTH_SHORT
                ).show();
            } else {
                // Fallback zu Toast wenn View nicht gefunden
                Toast.makeText(
                        this,
                        getString(messageResId, name),
                        Toast.LENGTH_SHORT
                ).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Fehler beim Anzeigen der Favoriten-Nachricht: " + e.getMessage());
        }
    }
    private void toggleTableVisibility() {
        isTableExpanded = !isTableExpanded;

        // Update Button text
        buttonExpandTable.setText(isTableExpanded ? R.string.table_hide : R.string.table_expand);

        // Animation für die Tabelle
        if (isTableExpanded) {
            // Tabelle einblenden mit Animation
            tableContainer.setVisibility(View.VISIBLE);
            tableContainer.setAlpha(0f);
            tableContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null);
        } else {
            // Tabelle ausblenden mit Animation
            tableContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            tableContainer.setVisibility(View.GONE);
                        }
                    });
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
            // Benutzerdefinierte Leets haben IDs ≥ 100
            int leetIndex = itemId - 100;

            // Debug-Logging
            Log.d("MainActivity", "Ausgewählter benutzerdefinierter Leet ID: " + itemId + ", Index: " + leetIndex);

            if (leetIndex >= 0 && leetIndex < profileManager.getProfiles().size()) {
                try {
                    setActiveMode(CUSTOM);
                    profileManager.setCurrentProfileIndex(leetIndex);

                    // Explizit die UI aktualisieren
                    updateOutput();
                    updateTable();
                    updateNavigationView();

                    // Aktualisiere den App-Titel gemäß des ausgewählten Profils
                    String profileName = profileManager.getCurrentProfile().getName();
                    appTitle.setText(profileName + " Leet");

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
        // Update checkmarks for current mode
        MenuItem simpleItem = navigationView.getMenu().findItem(R.id.nav_simple);
        MenuItem extendedItem = navigationView.getMenu().findItem(R.id.nav_extended);
        MenuItem aboutItem = navigationView.getMenu().findItem(R.id.nav_about);

        if (simpleItem != null) {
            simpleItem.setChecked(activeMode == SIMPLE);
            // Icon basierend auf Favoriten-Status
            boolean isSimpleFavorite = profileManager.isFavorite(SIMPLE, 0);
            Log.d("MainActivity", "Simple ist Favorit: " + isSimpleFavorite);
            simpleItem.setIcon(isSimpleFavorite ?
                    R.drawable.ic_simple_mode_favorite :
                    R.drawable.ic_simple_mode);
        }

        if (extendedItem != null) {
            extendedItem.setChecked(activeMode == EXTENDED);
            // Icon basierend auf Favoriten-Status
            boolean isExtendedFavorite = profileManager.isFavorite(EXTENDED, 0);
            Log.d("MainActivity", "Extended ist Favorit: " + isExtendedFavorite);
            extendedItem.setIcon(isExtendedFavorite ?
                    R.drawable.ic_extended_mode_favorite :
                    R.drawable.ic_extended_mode);
        }

        if (aboutItem != null) {
            aboutItem.setChecked(activeMode == ABOUT);
        }

        // Update custom profiles in menu
        updateCustomProfilesInMenu();

        // Aktualisiere Toolbar-Menü
        invalidateOptionsMenu();
    }



    // Keine direkten Änderungen am Code erforderlich, da die Gson-Bibliothek
// automatisch die neuen Felder in der CustomProfile-Klasse serialisiert/deserialisiert

// Wir müssen jedoch die Methode zum Aktualisieren der benutzerdefinierten Profile im Menü aktualisieren:

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

                // Entferne alle alten benutzerdefinierten Leets
                customSubMenu.clear();

                // Wenn keine benutzerdefinierten Leets existieren, zeige einen Hinweis an
                if (leets.isEmpty()) {
                    MenuItem noProfilesItem = customSubMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.no_custom_profiles);
                    noProfilesItem.setIcon(R.drawable.ic_custom_mode);
                    noProfilesItem.setEnabled(false);
                } else {
                    // Füge benutzerdefinierte Leets hinzu
                    for (int i = 0; i < leets.size(); i++) {
                        CustomProfile leet = leets.get(i);
                        MenuItem item = customSubMenu.add(Menu.NONE, 100 + i, Menu.NONE, leet.getName());

                        // Verwende das benutzerdefinierte Icon, falls vorhanden
                        int iconResId = leet.getIconResId();
                        if (iconResId != 0) {
                            // Setze Icon basierend auf dem Favoriten-Status
                            if (profileManager.isFavorite(CUSTOM, i)) {
                                // Für Favoriten das benutzerdefinierte Icon mit Stern-Overlay erstellen
                                Drawable originalIcon = getResources().getDrawable(iconResId, getTheme());
                                Drawable starIcon = getResources().getDrawable(R.drawable.ic_favorite_star, getTheme());

                                // Erstelle ein LayerDrawable (ähnlich wie ic_custom_mode_favorite.xml)
                                Drawable[] layers = new Drawable[2];
                                layers[0] = originalIcon;
                                layers[1] = starIcon;

                                LayerDrawable layerDrawable = new LayerDrawable(layers);

                                // Konfiguriere die Position des Stern-Icons (oben rechts)
                                int starSize = (int) getResources().getDimension(R.dimen.star_indicator_size);
                                layerDrawable.setLayerInset(1, originalIcon.getIntrinsicWidth() - starSize,
                                        0, 0, originalIcon.getIntrinsicHeight() - starSize);

                                item.setIcon(layerDrawable);
                            } else {
                                // Standard-Icon verwenden
                                item.setIcon(iconResId);
                            }
                        } else {
                            // Fallback zum Standard-Icon, wenn kein benutzerdefiniertes Icon gesetzt ist
                            if (profileManager.isFavorite(CUSTOM, i)) {
                                item.setIcon(R.drawable.ic_custom_mode_favorite);
                            } else {
                                item.setIcon(R.drawable.ic_custom_mode);
                            }
                        }

                        item.setCheckable(true);
                        item.setChecked(activeMode == CUSTOM && profileManager.getCurrentProfileIndex() == i);
                    }
                }
            }

            // Aktualisiere auch die Hauptmodi-Icons
            MenuItem simpleItem = menu.findItem(R.id.nav_simple);
            if (simpleItem != null) {
                if (profileManager.isFavorite(SIMPLE, 0)) {
                    simpleItem.setIcon(R.drawable.ic_simple_mode_favorite);
                } else {
                    simpleItem.setIcon(R.drawable.ic_simple_mode);
                }
            }

            MenuItem extendedItem = menu.findItem(R.id.nav_extended);
            if (extendedItem != null) {
                if (profileManager.isFavorite(EXTENDED, 0)) {
                    extendedItem.setIcon(R.drawable.ic_extended_mode_favorite);
                } else {
                    extendedItem.setIcon(R.drawable.ic_extended_mode);
                }
            }
        } catch (Exception e) {
            // Falls etwas schiefgeht, loggen wir den Fehler
            Log.e("MainActivity", "Fehler beim Aktualisieren der benutzerdefinierten Leets: " + e.getMessage());
            // Toast anzeigen, um den Benutzer zu informieren
            Toast.makeText(this, "Fehler beim Aktualisieren des Menüs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

// In der showNewProfileDialog-Methode aktualisierte Implementierung:
private void showNewProfileDialog() {
    // Erstelle einen MaterialAlertDialogBuilder
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

    // Lade das benutzerdefinierte Layout für den Dialog
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_profile, null);
    builder.setView(dialogView);

    // Erstelle den Dialog
    AlertDialog dialog = builder.create();

    // UI-Elemente im Dialog finden
    TextInputEditText editTextProfileName = dialogView.findViewById(R.id.editTextProfileName);
    ImageView selectedIcon = dialogView.findViewById(R.id.selectedIcon);
    Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
    Button buttonCreate = dialogView.findViewById(R.id.buttonCreate);

    // Variable für die ausgewählte Icon-Ressourcen-ID
    final int[] selectedIconResId = {R.drawable.ic_custom_mode}; // Standard-Icon

    // Event-Handler für Icon-Klick
    selectedIcon.setOnClickListener(v -> {
        IconSelectorDialog iconDialog = new IconSelectorDialog(
                MainActivity.this,
                (iconResId) -> {
                    selectedIconResId[0] = iconResId;
                    selectedIcon.setImageResource(iconResId);
                },
                selectedIconResId[0]
        );
        iconDialog.show();
    });

    // Event-Handler für Abbrechen-Button
    buttonCancel.setOnClickListener(v -> dialog.dismiss());

    // Event-Handler für Erstellen-Button
    buttonCreate.setOnClickListener(v -> {
        String leetName = editTextProfileName.getText().toString().trim();
        if (leetName.isEmpty()) {
            leetName = getString(R.string.default_custom_name);
        }

        // Erstelle neuen Leet mit den aktiven Buchstaben und dem ausgewählten Icon
        CustomProfile newLeet = new CustomProfile(leetName);
        newLeet.setIconResId(selectedIconResId[0]); // Setze das ausgewählte Icon

        // Initialisiere mit Buchstaben vom aktiven Modus
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
            updateTable();

            // Wichtig: Menü aktualisieren, um den neuen Leet anzuzeigen
            updateNavigationView();

            // Erfolgsmeldung
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.profile_created, leetName),
                    Snackbar.LENGTH_SHORT
            ).show();

            // Debug-Log
            Log.d("MainActivity", "Neuer Leet erstellt: " + leetName + ", Index: " +
                    (profileManager.getProfiles().size() - 1));
        } catch (Exception e) {
            Log.e("MainActivity", "Fehler beim Erstellen des Leets: " + e.getMessage());

            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(R.string.profile_creation_error, e.getMessage()),
                    Snackbar.LENGTH_LONG
            ).show();
        }

        dialog.dismiss();
    });

    // Dialog anzeigen
    dialog.show();

    // Fokus auf das Eingabefeld setzen und Tastatur anzeigen
    editTextProfileName.requestFocus();
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(editTextProfileName, InputMethodManager.SHOW_IMPLICIT);
}

    private void showDeleteProfileConfirmDialog() {
        // Prüfe zuerst, ob ein Custom-Profil existiert und ausgewählt ist
        CustomProfile currentProfile = profileManager.getCurrentProfile();
        if (currentProfile == null) {
            Toast.makeText(this, R.string.no_custom_profiles, Toast.LENGTH_SHORT).show();
            return;
        }

        // Verwende den Standard-Theme anstelle des benutzerdefinierten Themes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_confirm)
                .setMessage(getString(R.string.delete_confirm_message_named, currentProfile.getName()))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Hier den Fix: Richtig prüfen, ob das zu löschende Profil ein Favorit ist
                        boolean wasFavorite = profileManager.isFavorite(CUSTOM, profileManager.getCurrentProfileIndex());

                        // Profil löschen
                        profileManager.deleteCurrentProfile();

                        // Prüfe, ob noch Profile übrig sind
                        if (profileManager.hasProfiles()) {
                            // Wenn der gelöschte Leet ein Favorit war, zeige spezielle Nachricht an
                            if (wasFavorite) {
                                Snackbar.make(
                                        findViewById(android.R.id.content),
                                        R.string.favorite_deleted,
                                        Snackbar.LENGTH_LONG
                                ).show();
                            } else {
                                Snackbar.make(
                                        findViewById(android.R.id.content),
                                        R.string.profile_deleted,
                                        Snackbar.LENGTH_SHORT
                                ).show();
                            }

                            // Zum Simple Leet wechseln, wenn keine Custom Leets mehr übrig sind
                            if (!profileManager.hasProfiles()) {
                                setActiveMode(SIMPLE);
                            } else {
                                // Bleibe im Custom-Modus, aber aktualisiere UI
                                updateOutput();
                                updateTable();
                            }
                        } else {
                            // Keine Custom-Profile mehr verfügbar
                            Snackbar.make(
                                    findViewById(android.R.id.content),
                                    R.string.last_profile_deleted,
                                    Snackbar.LENGTH_SHORT
                            ).show();
                            // Zum Simple-Modus wechseln
                            setActiveMode(SIMPLE);
                        }

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
        // Prüfe, ob wir in den Custom Modus wechseln wollen, aber keine Profile existieren
        if (mode == CUSTOM && !profileManager.hasProfiles()) {
            // Wenn keine Custom Profile existieren, zeige einen Hinweis
            Toast.makeText(this, R.string.no_custom_profiles, Toast.LENGTH_SHORT).show();
            // Bleibe im aktuellen Modus
            return;
        }

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
                break;
            case EXTENDED:
                appTitle.setText(R.string.extended);
                break;
            case CUSTOM:
                // Hole den aktuellen Profilnamen und setze ihn als Titel
                CustomProfile currentProfile = profileManager.getCurrentProfile();
                if (currentProfile != null) {
                    String leetName = currentProfile.getName();
                    String titleText = leetName + " Leet";
                    appTitle.setText(titleText);
                } else {
                    // Fallback, falls kein Profil geladen werden konnte
                    appTitle.setText(R.string.custom);
                }
                break;
            case ABOUT:
                showAboutView();
                break;
        }

        // Exit edit mode if changing modes
        if (isEditMode) {
            isEditMode = false;
        }

        // Update navigation view
        updateNavigationView();

        // Update output and table
        updateOutput();
        updateTable();

        // Update Toolbar actions
        invalidateOptionsMenu();
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
                CustomProfile currentProfile = profileManager.getCurrentProfile();
                // Wenn kein Profil vorhanden ist, nutze Original-Charakter
                if (currentProfile == null) {
                    // Wenn kein Custom-Profil existiert, wechsle zum Simple-Modus
                    setActiveMode(SIMPLE);
                    return simpleLeetAlphabet[index];
                }
                return currentProfile.getTranslation(String.valueOf(upperChar));
            default:
                return String.valueOf(inputChar);
        }
    }

    private void updateTable() {
        // Clear the table
        leetTable.removeAllViews();

        // Tabellenzellen-Stil
        int cellPadding = (int) getResources().getDimension(R.dimen.table_cell_padding);

        // Hintergrundfarben für Zeilen
        int colorEven = getResources().getColor(R.color.gray_light, getTheme());
        int colorOdd = getResources().getColor(android.R.color.transparent, getTheme());

        // Add rows to the table
        for (int i = 0; i < 13; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? colorEven : colorOdd);

            // Füge Rand und Ripple-Effekt hinzu
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                row.setForeground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ripple_effect, getTheme()));
            }

            // Left side (letters 0-12)
            TextView leftPlain = createTableTextView(String.valueOf(plaintextAlphabet[i]), cellPadding);
            row.addView(leftPlain);

            // Left side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText leftLeet = createTableEditText(getTranslatedChar(plaintextAlphabet[i]), cellPadding);
                editableFields[i][0] = leftLeet;
                row.addView(leftLeet);
            } else {
                TextView leftLeet = createTableTextView(getTranslatedChar(plaintextAlphabet[i]), cellPadding);
                if (!isEditMode) {
                    displayFields[i][0] = leftLeet;
                }
                row.addView(leftLeet);
            }

            // Right side (letters 13-25)
            TextView rightPlain = createTableTextView(String.valueOf(plaintextAlphabet[i + 13]), cellPadding);
            row.addView(rightPlain);

            // Right side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText rightLeet = createTableEditText(getTranslatedChar(plaintextAlphabet[i + 13]), cellPadding);
                editableFields[i][1] = rightLeet;
                row.addView(rightLeet);
            } else {
                TextView rightLeet = createTableTextView(getTranslatedChar(plaintextAlphabet[i + 13]), cellPadding);
                if (!isEditMode) {
                    displayFields[i][1] = rightLeet;
                }
                row.addView(rightLeet);
            }

            leetTable.addView(row);
        }
    }

    // Hilfsmethode zum Erstellen von TextViews für die Tabelle
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

    // Hilfsmethode zum Erstellen von EditTexts für die Tabelle
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

        // Zeige den umfassenden Bearbeitungsdialog an
        showComprehensiveEditDialog();

        // Wir setzen isEditMode nicht mehr, da die Bearbeitung jetzt vollständig im Dialog stattfindet
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

    private void showComprehensiveEditDialog() {
        // Hole das aktuelle Profil
        CustomProfile currentProfile = profileManager.getCurrentProfile();

        // Wenn kein Profil vorhanden ist, zeige eine Meldung an und breche ab
        if (currentProfile == null) {
            Toast.makeText(this, R.string.no_custom_profiles_available, Toast.LENGTH_SHORT).show();
            return;
        }

        // Erstelle einen MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Lade das benutzerdefinierte Layout für den Dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comprehensive_edit, null);
        builder.setView(dialogView);

        // Erstelle den Dialog
        AlertDialog dialog = builder.create();

        // UI-Elemente im Dialog finden
        TextInputEditText editTextProfileName = dialogView.findViewById(R.id.editTextProfileName);
        ImageView selectedIcon = dialogView.findViewById(R.id.selectedIcon);
        TableLayout editTable = dialogView.findViewById(R.id.editTable);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);

        // Fülle die Felder mit den aktuellen Werten
        editTextProfileName.setText(currentProfile.getName());

        // Icon setzen, falls vorhanden
        int iconResId = currentProfile.getIconResId();
        if (iconResId != 0) {
            selectedIcon.setImageResource(iconResId);
        }

        // Variable für die ausgewählte Icon-Ressourcen-ID
        final int[] selectedIconResId = {iconResId};

        // Event-Handler für Icon-Klick
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

        // Initialisiere die Bearbeitungsfelder für die Tabelle
        EditText[][] tableEditFields = new EditText[13][2];

        // Tabellenzellen-Stil
        int cellPadding = (int) getResources().getDimension(R.dimen.table_cell_padding);

        // Hintergrundfarben für Zeilen
        int colorEven = getResources().getColor(R.color.gray_light, getTheme());
        int colorOdd = getResources().getColor(android.R.color.transparent, getTheme());

        // Füge Zeilen zur Tabelle hinzu
        for (int i = 0; i < 13; i++) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(i % 2 == 0 ? colorEven : colorOdd);

            // Füge Rand und Ripple-Effekt hinzu
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                row.setForeground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ripple_effect, getTheme()));
            }

            // Linke Seite (Buchstaben 0-12)
            TextView leftPlain = createTableTextView(String.valueOf(plaintextAlphabet[i]), cellPadding);
            row.addView(leftPlain);

            // Linke Seite Leet-Zeichen (editierbar)
            EditText leftLeet = createTableEditText(currentProfile.getTranslation(String.valueOf(plaintextAlphabet[i])), cellPadding);
            tableEditFields[i][0] = leftLeet;
            row.addView(leftLeet);

            // Rechte Seite (Buchstaben 13-25)
            TextView rightPlain = createTableTextView(String.valueOf(plaintextAlphabet[i + 13]), cellPadding);
            row.addView(rightPlain);

            // Rechte Seite Leet-Zeichen (editierbar)
            EditText rightLeet = createTableEditText(currentProfile.getTranslation(String.valueOf(plaintextAlphabet[i + 13])), cellPadding);
            tableEditFields[i][1] = rightLeet;
            row.addView(rightLeet);

            editTable.addView(row);
        }

        // Event-Handler für Abbrechen-Button
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        // Event-Handler für Speichern-Button
        buttonSave.setOnClickListener(v -> {
            String profileName = editTextProfileName.getText().toString().trim();
            if (profileName.isEmpty()) {
                profileName = getString(R.string.default_custom_name);
            }

            try {
                // Aktualisiere das Profil
                currentProfile.setName(profileName);
                currentProfile.setIconResId(selectedIconResId[0]);

                // Aktualisiere die Übersetzungstabelle
                for (int i = 0; i < 13; i++) {
                    // Linke Seite (Buchstaben 0-12)
                    String plainChar = String.valueOf(plaintextAlphabet[i]);
                    String leetChar = tableEditFields[i][0].getText().toString();
                    currentProfile.setTranslation(plainChar, leetChar);

                    // Rechte Seite (Buchstaben 13-25)
                    plainChar = String.valueOf(plaintextAlphabet[i + 13]);
                    leetChar = tableEditFields[i][1].getText().toString();
                    currentProfile.setTranslation(plainChar, leetChar);
                }

                // Speichere die Änderungen
                profileManager.updateCurrentProfile(currentProfile);

                // UI aktualisieren
                appTitle.setText(profileName + " Leet");
                updateNavigationView();
                updateOutput();
                updateTable();

                // Erfolgsmeldung
                Snackbar.make(
                        findViewById(android.R.id.content),
                        R.string.all_changes_saved,
                        Snackbar.LENGTH_SHORT
                ).show();

            } catch (Exception e) {
                Log.e("MainActivity", "Fehler beim Aktualisieren des Profils: " + e.getMessage());

                Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.profile_update_error, e.getMessage()),
                        Snackbar.LENGTH_LONG
                ).show();
            }

            dialog.dismiss();
        });

        // Dialog anzeigen
        dialog.show();

        // Fokus auf das Eingabefeld setzen und Tastatur anzeigen
        editTextProfileName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextProfileName, InputMethodManager.SHOW_IMPLICIT);
    }
    }