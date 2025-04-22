package com.beigel.leetSpeak_Generator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SIMPLE = 0;
    private static final int EXTENDED = 1;
    private static final int CUSTOM = 2;

    private final char[] plaintextAlphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    private final String[] simpleLeetAlphabet = {"4", "8", "C", "D", "3", "F", "6", "#", "1", "J", "K", "L", "M", "N", "0", "P", "Q", "R", "5", "7", "U", "V", "W", "X", "Y", "2"};

    private final String[] extendedLeetAlphabet = {"4", "8", "(", "|)", "3", "|=", "6", "#", "!", "_|", "|<", "1", "/\\/\\", "|\\|", "0", "9", "0_", "2", "5", "7", "|_|", "\\/", "\\/\\/", "><", "`/", "Z"};

    private int activeMode = SIMPLE;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText inputPlainText;
    private TextView outputLeetText;
    private Button buttonCopy;
    private Button buttonEdit;
    private Button buttonSave;
    private TextView tableTitle;
    private TableLayout leetTable;

    private ProfileManager profileManager;
    private EditText[][] editableFields = new EditText[13][2];
    private TextView[][] displayFields = new TextView[13][2];
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonSave = findViewById(R.id.buttonSave);
        tableTitle = findViewById(R.id.tableTitle);
        leetTable = findViewById(R.id.leetTable);

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
        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToEditMode();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomTranslations();
            }
        });

        // Set initial mode and update UI
        setActiveMode(SIMPLE);
        updateNavigationView();
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
            profileManager.setCurrentProfileIndex(0); // Select the default custom profile
            updateTableTitle();
            updateOutput();
        } else if (itemId == R.id.nav_new_custom) {
            showNewProfileDialog();
        } else if (itemId == R.id.nav_save_custom) {
            if (activeMode == CUSTOM) {
                saveCurrentCustomProfile();
            }
        } else if (itemId == R.id.nav_delete_custom) {
            if (activeMode == CUSTOM && profileManager.canDeleteCurrentProfile()) {
                showDeleteProfileConfirmDialog();
            } else {
                Toast.makeText(this, R.string.default_profile_no_delete, Toast.LENGTH_SHORT).show();
            }
        } else if (item.getGroupId() == R.id.nav_group_custom_profiles) {
            // Handle custom profile selection for dynamically created menu items
            int profileIndex = item.getItemId() - 100; // We used IDs starting from 100 for custom profiles
            if (profileIndex >= 0 && profileIndex < profileManager.getProfiles().size()) {
                setActiveMode(CUSTOM);
                profileManager.setCurrentProfileIndex(profileIndex);
                updateTableTitle();
                updateOutput();
                updateTable();
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void updateNavigationView() {
        // Update visibility of save/delete options
        MenuItem saveItem = navigationView.getMenu().findItem(R.id.nav_save_custom);
        MenuItem deleteItem = navigationView.getMenu().findItem(R.id.nav_delete_custom);

        if (saveItem != null) {
            saveItem.setVisible(activeMode == CUSTOM);
        }

        if (deleteItem != null) {
            deleteItem.setVisible(activeMode == CUSTOM && profileManager.canDeleteCurrentProfile());
        }

        // Update checkmarks for current mode
        MenuItem simpleItem = navigationView.getMenu().findItem(R.id.nav_simple);
        MenuItem extendedItem = navigationView.getMenu().findItem(R.id.nav_extended);
        MenuItem customDefaultItem = navigationView.getMenu().findItem(R.id.nav_custom_default);

        if (simpleItem != null) {
            simpleItem.setChecked(activeMode == SIMPLE);
        }

        if (extendedItem != null) {
            extendedItem.setChecked(activeMode == EXTENDED);
        }

        if (customDefaultItem != null) {
            customDefaultItem.setChecked(activeMode == CUSTOM && profileManager.getCurrentProfileIndex() == 0);
        }

        // Update custom profiles in menu
        updateCustomProfilesInMenu();
    }

    private void updateCustomProfilesInMenu() {
        // Get the custom profiles
        List<CustomProfile> profiles = profileManager.getProfiles();

        // Find the submenu that contains the custom profiles
        NavigationView navigationView = findViewById(R.id.nav_view);
        android.view.Menu menu = navigationView.getMenu();

        // Remove old custom profile items
        // Starting from index 1 to keep the default custom profile
        int customProfilesGroupId = R.id.nav_group_custom_profiles;
        menu.removeGroup(customProfilesGroupId);

        // Add custom profiles submenu items
        for (int i = 1; i < profiles.size(); i++) {  // Starting from 1 to skip default profile
            CustomProfile profile = profiles.get(i);
            MenuItem item = menu.add(customProfilesGroupId, 100 + i, i, profile.getName());
            item.setIcon(R.drawable.ic_custom_mode);
            item.setCheckable(true);
            item.setChecked(activeMode == CUSTOM && profileManager.getCurrentProfileIndex() == i);
        }
    }

    private void showNewProfileDialog() {
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
                String profileName = input.getText().toString().trim();
                if (profileName.isEmpty()) {
                    profileName = getString(R.string.default_custom_name);
                }

                // Create new profile with current letters as default
                CustomProfile newProfile = new CustomProfile(profileName);

                // Initialize with current letters from active mode
                for (char c : plaintextAlphabet) {
                    String plainChar = String.valueOf(c);
                    String leetChar = getTranslatedChar(c);
                    newProfile.setTranslation(plainChar, leetChar);
                }

                profileManager.addProfile(newProfile);
                setActiveMode(CUSTOM);
                updateTableTitle();
                updateNavigationView();
                updateTable();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_confirm)
                .setMessage(R.string.delete_confirm_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        profileManager.deleteCurrentProfile();
                        Toast.makeText(MainActivity.this, R.string.profile_deleted, Toast.LENGTH_SHORT).show();
                        updateTableTitle();
                        updateNavigationView();
                        updateTable();
                        updateOutput();
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

        // Update UI based on mode
        switch (mode) {
            case SIMPLE:
                tableTitle.setText(R.string.simple_table_title);
                buttonEdit.setVisibility(View.GONE);
                buttonSave.setVisibility(View.GONE);
                break;
            case EXTENDED:
                tableTitle.setText(R.string.extended_table_title);
                buttonEdit.setVisibility(View.GONE);
                buttonSave.setVisibility(View.GONE);
                break;
            case CUSTOM:
                updateTableTitle();
                buttonEdit.setVisibility(View.VISIBLE);
                buttonSave.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
                break;
        }

        // Exit edit mode if changing modes
        if (isEditMode) {
            isEditMode = false;
            buttonSave.setVisibility(View.GONE);
        }

        // Update placeholder
        String placeholder = getString(R.string.plaintext_placeholder);
        inputPlainText.setHint(placeholder);

        // Update navigation view
        updateNavigationView();

        // Update output and table
        updateOutput();
        updateTable();
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

        // Add rows to the table
        for (int i = 0; i < 13; i++) {
            TableRow row = new TableRow(this);

            // Left side (letters 0-12)
            TextView leftPlain = new TextView(this);
            leftPlain.setText(String.valueOf(plaintextAlphabet[i]));
            leftPlain.setPadding(10, 10, 10, 10);
            row.addView(leftPlain);

            // Left side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText leftLeet = new EditText(this);
                leftLeet.setText(getTranslatedChar(plaintextAlphabet[i]));
                editableFields[i][0] = leftLeet;
                row.addView(leftLeet);
            } else {
                TextView leftLeet = new TextView(this);
                leftLeet.setText(getTranslatedChar(plaintextAlphabet[i]));
                leftLeet.setPadding(10, 10, 20, 10);
                if (!isEditMode) {
                    displayFields[i][0] = leftLeet;
                }
                row.addView(leftLeet);
            }

            // Right side (letters 13-25)
            TextView rightPlain = new TextView(this);
            rightPlain.setText(String.valueOf(plaintextAlphabet[i + 13]));
            rightPlain.setPadding(20, 10, 10, 10);
            row.addView(rightPlain);

            // Right side leet character
            if (isEditMode && activeMode == CUSTOM) {
                EditText rightLeet = new EditText(this);
                rightLeet.setText(getTranslatedChar(plaintextAlphabet[i + 13]));
                editableFields[i][1] = rightLeet;
                row.addView(rightLeet);
            } else {
                TextView rightLeet = new TextView(this);
                rightLeet.setText(getTranslatedChar(plaintextAlphabet[i + 13]));
                rightLeet.setPadding(10, 10, 10, 10);
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
        buttonEdit.setVisibility(View.GONE);
        buttonSave.setVisibility(View.VISIBLE);
        updateTable();

        // Update save option in navigation menu
        MenuItem saveItem = navigationView.getMenu().findItem(R.id.nav_save_custom);
        if (saveItem != null) {
            saveItem.setVisible(true);
        }
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
        buttonEdit.setVisibility(View.VISIBLE);
        buttonSave.setVisibility(View.GONE);

        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();

        updateTable();
        updateOutput();

        // Hide save menu option
        MenuItem saveItem = navigationView.getMenu().findItem(R.id.nav_save_custom);
        if (saveItem != null) {
            saveItem.setVisible(false);
        }
    }

    private void copyToClipboard() {
        String text = outputLeetText.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Leetspeak Text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show();
    }
}