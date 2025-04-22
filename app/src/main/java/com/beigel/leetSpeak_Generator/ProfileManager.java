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
    private static final String PROFILES_KEY = "profiles";
    private static final String CURRENT_PROFILE_KEY = "current_profile";

    private final SharedPreferences prefs;
    private final Gson gson;
    private List<CustomProfile> profiles;
    private int currentProfileIndex;

    public ProfileManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadProfiles();
    }

    private void loadProfiles() {
        String profilesJson = prefs.getString(PROFILES_KEY, null);
        currentProfileIndex = prefs.getInt(CURRENT_PROFILE_KEY, 0);

        if (profilesJson == null) {
            // Create default profile if none exists
            profiles = new ArrayList<>();
            CustomProfile defaultProfile = new CustomProfile("Custom");
            // Initialize with default translations (identity mapping)
            for (char c = 'A'; c <= 'Z'; c++) {
                defaultProfile.setTranslation(String.valueOf(c), String.valueOf(c));
            }
            profiles.add(defaultProfile);
            saveProfiles();
        } else {
            Type type = new TypeToken<ArrayList<CustomProfile>>(){}.getType();
            profiles = gson.fromJson(profilesJson, type);

            // Ensure we have at least one profile
            if (profiles.isEmpty()) {
                CustomProfile defaultProfile = new CustomProfile("Custom");
                for (char c = 'A'; c <= 'Z'; c++) {
                    defaultProfile.setTranslation(String.valueOf(c), String.valueOf(c));
                }
                profiles.add(defaultProfile);
                saveProfiles();
            }

            // Make sure currentProfileIndex is valid
            if (currentProfileIndex < 0 || currentProfileIndex >= profiles.size()) {
                currentProfileIndex = 0;
            }
        }
    }

    public void saveProfiles() {
        String profilesJson = gson.toJson(profiles);
        prefs.edit()
                .putString(PROFILES_KEY, profilesJson)
                .putInt(CURRENT_PROFILE_KEY, currentProfileIndex)
                .apply();
    }

    public List<CustomProfile> getProfiles() {
        return profiles;
    }

    public CustomProfile getCurrentProfile() {
        if (profiles.isEmpty()) {
            // This should not happen, but let's be safe
            CustomProfile defaultProfile = new CustomProfile("Custom");
            for (char c = 'A'; c <= 'Z'; c++) {
                defaultProfile.setTranslation(String.valueOf(c), String.valueOf(c));
            }
            profiles.add(defaultProfile);
            saveProfiles();
        }
        return profiles.get(currentProfileIndex);
    }

    public void setCurrentProfileIndex(int index) {
        if (index >= 0 && index < profiles.size()) {
            currentProfileIndex = index;
            saveProfiles();
        }
    }

    public int getCurrentProfileIndex() {
        return currentProfileIndex;
    }

    public void addProfile(CustomProfile profile) {
        profiles.add(profile);
        currentProfileIndex = profiles.size() - 1; // Switch to the new profile
        saveProfiles();
    }

    public void updateCurrentProfile(CustomProfile profile) {
        if (currentProfileIndex >= 0 && currentProfileIndex < profiles.size()) {
            profiles.set(currentProfileIndex, profile);
            saveProfiles();
        }
    }

    public void deleteCurrentProfile() {
        if (profiles.size() > 1 && currentProfileIndex >= 0 && currentProfileIndex < profiles.size()) {
            profiles.remove(currentProfileIndex);
            if (currentProfileIndex >= profiles.size()) {
                currentProfileIndex = profiles.size() - 1;
            }
            saveProfiles();
        }
    }

    public boolean canDeleteCurrentProfile() {
        // Don't allow deleting the last profile or the default profile (index 0)
        return profiles.size() > 1 && currentProfileIndex > 0;
    }
}