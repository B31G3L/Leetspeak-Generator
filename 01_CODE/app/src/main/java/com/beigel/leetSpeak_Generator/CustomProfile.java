package com.beigel.leetSpeak_Generator;

import java.util.HashMap;
import java.util.Map;

public class CustomProfile {
    private String name;
    private Map<String, String> translations;
    private int iconResId; // Neue Variable für die Icon-Ressourcen-ID

    public CustomProfile(String name) {
        this.name = name;
        this.translations = new HashMap<>();
        this.iconResId = R.drawable.ic_custom_mode; // Standard-Icon als Fallback
    }

    public CustomProfile(String name, int iconResId) {
        this.name = name;
        this.translations = new HashMap<>();
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }

    public void setTranslation(String plainChar, String leetChar) {
        translations.put(plainChar, leetChar);
    }

    public String getTranslation(String plainChar) {
        return translations.getOrDefault(plainChar, plainChar);
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}