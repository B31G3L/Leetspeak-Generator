package com.beigel.leetSpeak_Generator;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    private static final String PREFS_NAME = "LeetSpeakProfiles";
    private static final String LEETS_KEY = "leets"; // Geändert von "profiles"
    private static final String CURRENT_LEET_KEY = "current_leet"; // Geändert von "current_profile"

    private final SharedPreferences prefs;
    private final Gson gson;
    private List<CustomProfile> leets; // Geändert von "profiles"
    private int currentLeetIndex; // Geändert von "currentProfileIndex"

    public ProfileManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadLeets(); // Geändert von "loadProfiles"
    }

    private void loadLeets() { // Geändert von "loadProfiles"
        String leetsJson = prefs.getString(LEETS_KEY, null); // Geändert von "profilesJson"
        currentLeetIndex = prefs.getInt(CURRENT_LEET_KEY, 0); // Geändert von "currentProfileIndex"

        if (leetsJson == null) {
            // Erstelle Standard-Leet, wenn keines existiert
            leets = new ArrayList<>();
            CustomProfile defaultLeet = new CustomProfile("Custom"); // Terminologie bleibt hier als "CustomProfile"
            // Initialisiere mit Standard-Übersetzungen (identische Zuordnung)
            for (char c = 'A'; c <= 'Z'; c++) {
                defaultLeet.setTranslation(String.valueOf(c), String.valueOf(c));
            }
            leets.add(defaultLeet);
            saveLeets(); // Geändert von "saveProfiles"
        } else {
            Type type = new TypeToken<ArrayList<CustomProfile>>(){}.getType();
            leets = gson.fromJson(leetsJson, type);

            // Stelle sicher, dass wir mindestens ein Leet haben
            if (leets.isEmpty()) {
                CustomProfile defaultLeet = new CustomProfile("Custom");
                for (char c = 'A'; c <= 'Z'; c++) {
                    defaultLeet.setTranslation(String.valueOf(c), String.valueOf(c));
                }
                leets.add(defaultLeet);
                saveLeets(); // Geändert von "saveProfiles"
            }

            // Stelle sicher, dass currentLeetIndex gültig ist
            if (currentLeetIndex < 0 || currentLeetIndex >= leets.size()) {
                currentLeetIndex = 0;
            }
        }
    }

    public void saveLeets() { // Geändert von "saveProfiles"
        String leetsJson = gson.toJson(leets);
        prefs.edit()
                .putString(LEETS_KEY, leetsJson)
                .putInt(CURRENT_LEET_KEY, currentLeetIndex)
                .apply();
    }

    public List<CustomProfile> getProfiles() {
        return leets; // Geändert von "profiles", Methodenname bleibt für Kompatibilität
    }

    public CustomProfile getCurrentProfile() {
        if (leets.isEmpty()) {
            // Dies sollte nicht passieren, aber für die Sicherheit
            CustomProfile defaultLeet = new CustomProfile("Custom");
            for (char c = 'A'; c <= 'Z'; c++) {
                defaultLeet.setTranslation(String.valueOf(c), String.valueOf(c));
            }
            leets.add(defaultLeet);
            saveLeets();
        }
        return leets.get(currentLeetIndex);
    }

    public void setCurrentProfileIndex(int index) {
        if (index >= 0 && index < leets.size()) {
            currentLeetIndex = index;
            saveLeets();
        }
    }

    public int getCurrentProfileIndex() {
        return currentLeetIndex;
    }

    public void addProfile(CustomProfile profile) {
        leets.add(profile);
        currentLeetIndex = leets.size() - 1; // Wechsle zum neuen Leet
        saveLeets();
    }

    public void updateCurrentProfile(CustomProfile profile) {
        if (currentLeetIndex >= 0 && currentLeetIndex < leets.size()) {
            leets.set(currentLeetIndex, profile);
            saveLeets();
        }
    }

    public void deleteCurrentProfile() {
        if (leets.size() > 1 && currentLeetIndex >= 0 && currentLeetIndex < leets.size()) {
            leets.remove(currentLeetIndex);
            if (currentLeetIndex >= leets.size()) {
                currentLeetIndex = leets.size() - 1;
            }
            saveLeets();
        }
    }

    public boolean canDeleteCurrentProfile() {
        // Erlaube nicht das Löschen des letzten Leets oder des Standard-Leets (Index 0)
        return leets.size() > 1 && currentLeetIndex > 0;
    }
}