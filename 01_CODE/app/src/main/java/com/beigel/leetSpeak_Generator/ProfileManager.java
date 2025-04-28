package com.beigel.leetSpeak_Generator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    private static final String PREFS_NAME = "LeetSpeakProfiles";
    private static final String LEETS_KEY = "leets";
    private static final String CURRENT_LEET_KEY = "current_leet";
    private static final String FAVORITE_LEET_KEY = "favorite_leet";

    // Modus-Konstanten, die in MainActivity.java verwendet werden sollten
    public static final int MODE_SIMPLE = 0;
    public static final int MODE_EXTENDED = 1;
    public static final int MODE_CUSTOM = 2;
    public static final int MODE_ABOUT = 3;

    // Spezielle Konstanten für Favoriten-Indizes
    public static final int FAV_NONE = -1;       // Kein Favorit
    public static final int FAV_SIMPLE = -100;   // Simple Leet
    public static final int FAV_EXTENDED = -101; // Extended Leet

    private final SharedPreferences prefs;
    private final Gson gson;
    private List<CustomProfile> leets;
    private int currentLeetIndex;
    private int favoriteLeetIndex = FAV_NONE; // -1 bedeutet kein Favorit

    public ProfileManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadLeets();
    }

    private void loadLeets() {
        String leetsJson = prefs.getString(LEETS_KEY, null);
        currentLeetIndex = prefs.getInt(CURRENT_LEET_KEY, 0);
        favoriteLeetIndex = prefs.getInt(FAVORITE_LEET_KEY, FAV_NONE);

        if (leetsJson == null) {
            // Erstelle Standard-Leet, wenn keines existiert
            leets = new ArrayList<>();
            CustomProfile defaultLeet = new CustomProfile("Custom");
            // Initialisiere mit Standard-Übersetzungen (identische Zuordnung)
            for (char c = 'A'; c <= 'Z'; c++) {
                defaultLeet.setTranslation(String.valueOf(c), String.valueOf(c));
            }
            leets.add(defaultLeet);
            saveLeets();
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
                saveLeets();
            }

            // Stelle sicher, dass currentLeetIndex gültig ist
            if (currentLeetIndex < 0 || currentLeetIndex >= leets.size()) {
                currentLeetIndex = 0;
            }
        }

        // Migration von alten Favoriten-Werten
        if (favoriteLeetIndex >= 0 && favoriteLeetIndex < leets.size()) {
            // Der alte Wert war ein positiver Index, der direkt auf ein Custom-Profil verweist
            // Wir belassen ihn, wie er ist
            Log.d("ProfileManager", "Favoriten-Index geladen (Custom): " + favoriteLeetIndex);
        } else if (favoriteLeetIndex == 0) {
            // Könnte Simple oder Custom Default sein - wir gehen von Simple aus
            favoriteLeetIndex = FAV_SIMPLE;
            saveFavoriteIndex();
            Log.d("ProfileManager", "Favoriten-Index migriert von 0 zu FAV_SIMPLE: " + FAV_SIMPLE);
        } else if (favoriteLeetIndex == 1) {
            // Könnte Extended oder 2. Custom sein - wir gehen von Extended aus
            favoriteLeetIndex = FAV_EXTENDED;
            saveFavoriteIndex();
            Log.d("ProfileManager", "Favoriten-Index migriert von 1 zu FAV_EXTENDED: " + FAV_EXTENDED);
        }

        // Debug Log
        Log.d("ProfileManager", "Favoriten-Index geladen: " + favoriteLeetIndex);
    }

    private void saveFavoriteIndex() {
        prefs.edit()
                .putInt(FAVORITE_LEET_KEY, favoriteLeetIndex)
                .apply();

        // Debug Log
        Log.d("ProfileManager", "Favoriten-Index gespeichert: " + favoriteLeetIndex);
    }

    public void saveLeets() {
        String leetsJson = gson.toJson(leets);
        prefs.edit()
                .putString(LEETS_KEY, leetsJson)
                .putInt(CURRENT_LEET_KEY, currentLeetIndex)
                .apply();

        // Achten Sie darauf, dass der Favoriten-Index separat gespeichert wird
        saveFavoriteIndex();
    }

    public List<CustomProfile> getProfiles() {
        return leets;
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

    // Methoden für Favoriten-Funktionalität

    // Konvertiert einen Modus-Index in einen Favoriten-Index
    public int getModeAsFavoriteIndex(int mode, int customIndex) {
        switch (mode) {
            case MODE_SIMPLE:
                return FAV_SIMPLE;
            case MODE_EXTENDED:
                return FAV_EXTENDED;
            case MODE_CUSTOM:
                return customIndex; // Für Custom-Mode verwenden wir den Index des Profils
            default:
                return FAV_NONE;
        }
    }

    public int getFavoriteLeetIndex() {
        return favoriteLeetIndex;
    }

    public int getFavoriteMode() {
        if (favoriteLeetIndex == FAV_SIMPLE) {
            return MODE_SIMPLE;
        } else if (favoriteLeetIndex == FAV_EXTENDED) {
            return MODE_EXTENDED;
        } else if (favoriteLeetIndex >= 0) {
            return MODE_CUSTOM;
        } else {
            return -1; // Kein Favorit
        }
    }

    public int getFavoriteCustomIndex() {
        if (favoriteLeetIndex >= 0) {
            return favoriteLeetIndex;
        }
        return -1; // Kein Custom-Favorit
    }

    public void setFavoriteLeet(int index) {
        favoriteLeetIndex = index;
        saveFavoriteIndex();
        Log.d("ProfileManager", "Favorit gesetzt auf: " + index);
    }

    public boolean isFavorite(int mode, int customIndex) {
        if (mode == MODE_SIMPLE) {
            return favoriteLeetIndex == FAV_SIMPLE;
        } else if (mode == MODE_EXTENDED) {
            return favoriteLeetIndex == FAV_EXTENDED;
        } else if (mode == MODE_CUSTOM) {
            return favoriteLeetIndex == customIndex;
        }
        return false;
    }

    // Methode zum Umschalten des Favoriten-Status
    public void toggleFavorite(int mode, int customIndex) {
        Log.d("ProfileManager", "toggleFavorite aufgerufen mit Mode: " + mode +
                ", customIndex: " + customIndex +
                ", aktueller Favorit: " + favoriteLeetIndex);

        int newFavoriteIndex;

        if (mode == MODE_SIMPLE) {
            newFavoriteIndex = FAV_SIMPLE;
        } else if (mode == MODE_EXTENDED) {
            newFavoriteIndex = FAV_EXTENDED;
        } else if (mode == MODE_CUSTOM) {
            newFavoriteIndex = customIndex;
        } else {
            return; // Ungültiger Modus
        }

        // Wenn bereits Favorit, dann entfernen
        if (favoriteLeetIndex == newFavoriteIndex) {
            favoriteLeetIndex = FAV_NONE;
            Log.d("ProfileManager", "Favorit entfernt");
        } else {
            // Ansonsten als Favorit setzen
            favoriteLeetIndex = newFavoriteIndex;
            Log.d("ProfileManager", "Favorit gesetzt auf: " + newFavoriteIndex);
        }

        saveFavoriteIndex();
    }

    public void deleteCurrentProfile() {
        if (leets.size() > 1 && currentLeetIndex > 0 && currentLeetIndex < leets.size()) {
            // Wenn der zu löschende Leet ein Favorit ist
            if (currentLeetIndex == favoriteLeetIndex) {
                // Favorit zurücksetzen (kein Favorit)
                favoriteLeetIndex = FAV_NONE;
                Log.d("ProfileManager", "Favorit wurde zurückgesetzt, da er gelöscht wurde");
            } else if (favoriteLeetIndex > currentLeetIndex) {
                // Wenn der Favorit nach dem gelöschten Leet kommt, Index anpassen
                favoriteLeetIndex--;
                Log.d("ProfileManager", "Favoriten-Index angepasst auf: " + favoriteLeetIndex);
            }

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

    // Kompatibilitätsmethoden für alte Aufrufe
    public boolean isFavorite(int index) {
        // Kompatibilitätsmethode für alte Aufrufe
        Log.d("ProfileManager", "Veralteter Aufruf von isFavorite(int) mit Index: " + index);

        if (index == MODE_SIMPLE || index == 0) {
            // Könnte Simple oder Custom-Standard sein - wir prüfen beide
            return favoriteLeetIndex == FAV_SIMPLE ||
                    (index == 0 && favoriteLeetIndex == 0);
        } else if (index == MODE_EXTENDED || index == 1) {
            // Könnte Extended oder 2. Custom sein - wir prüfen beide
            return favoriteLeetIndex == FAV_EXTENDED ||
                    (index == 1 && favoriteLeetIndex == 1);
        } else {
            // Muss ein Custom-Profil sein
            return favoriteLeetIndex == index;
        }
    }

    public void toggleFavorite(int index) {
        // Kompatibilitätsmethode für alte Aufrufe
        Log.d("ProfileManager", "Veralteter Aufruf von toggleFavorite(int) mit Index: " + index);

        if (index == MODE_SIMPLE) {
            toggleFavorite(MODE_SIMPLE, 0);
        } else if (index == MODE_EXTENDED) {
            toggleFavorite(MODE_EXTENDED, 0);
        } else {
            // Muss ein Custom-Profil sein
            toggleFavorite(MODE_CUSTOM, index);
        }
    }
}