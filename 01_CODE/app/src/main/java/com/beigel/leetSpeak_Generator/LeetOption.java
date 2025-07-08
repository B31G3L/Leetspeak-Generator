package com.beigel.leetSpeak_Generator;

public class LeetOption {
    private final int mode;
    private final String name;
    private final String description;
    private final int iconResId;
    private final boolean isCustom;
    private final int customIndex;
    private boolean isSelected;
    private boolean isFavorite;

    public LeetOption(int mode, String name, String description, int iconResId,
                      boolean isCustom, int customIndex, boolean isSelected, boolean isFavorite) {
        this.mode = mode;
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.isCustom = isCustom;
        this.customIndex = customIndex;
        this.isSelected = isSelected;
        this.isFavorite = isFavorite;
    }

    // Getters
    public int getMode() { return mode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
    public boolean isCustom() { return isCustom; }
    public int getCustomIndex() { return customIndex; }
    public boolean isSelected() { return isSelected; }
    public boolean isFavorite() { return isFavorite; }

    // Setters
    public void setSelected(boolean selected) { isSelected = selected; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}