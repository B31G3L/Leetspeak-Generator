package com.beigel.leetSpeak_Generator;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileRepository {
    private final ProfileManager profileManager;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public ProfileRepository(Context context) {
        this.profileManager = new ProfileManager(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void loadFavoriteMode(FavoriteModeCallback callback) {
        executorService.execute(() -> {
            try {
                FavoriteModeResult result = loadFavoriteModeSync();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    private FavoriteModeResult loadFavoriteModeSync() {
        int favoriteMode = profileManager.getFavoriteMode();
        int favoriteCustomIndex = profileManager.getFavoriteCustomIndex();

        if (favoriteMode >= 0) {
            if (favoriteMode == ProfileManager.MODE_SIMPLE) {
                return FavoriteModeResult.simple();
            } else if (favoriteMode == ProfileManager.MODE_EXTENDED) {
                return FavoriteModeResult.extended();
            } else if (favoriteMode == ProfileManager.MODE_CUSTOM && favoriteCustomIndex >= 0) {
                if (profileManager.hasProfiles() && favoriteCustomIndex < profileManager.getProfiles().size()) {
                    CustomProfile profile = profileManager.getProfiles().get(favoriteCustomIndex);
                    return FavoriteModeResult.custom(favoriteCustomIndex, profile);
                }
            }
        }

        return FavoriteModeResult.simple();
    }

    public void createProfile(ProfileCreationRequest request, ProfileOperationCallback callback) {
        executorService.execute(() -> {
            try {
                CustomProfile newProfile = new CustomProfile(request.getName());
                newProfile.setIconResId(request.getIconResId());

                for (Map.Entry<String, String> entry : request.getTranslations().entrySet()) {
                    newProfile.setTranslation(entry.getKey(), entry.getValue());
                }

                profileManager.addProfile(newProfile);
                int newIndex = profileManager.getProfiles().size() - 1;

                ProfileOperationResult result = ProfileOperationResult.success(
                        "Profil erfolgreich erstellt", newProfile, newIndex);

                mainHandler.post(() -> callback.onComplete(result));

            } catch (Exception e) {
                ProfileOperationResult result = ProfileOperationResult.error(
                        "Fehler beim Erstellen des Profils", e);
                mainHandler.post(() -> callback.onComplete(result));
            }
        });
    }

    public void deleteProfile(int profileIndex, ProfileOperationCallback callback) {
        executorService.execute(() -> {
            try {
                if (profileIndex < 0 || profileIndex >= profileManager.getProfiles().size()) {
                    throw new IllegalArgumentException("Ungültiger Profil-Index");
                }

                CustomProfile profileToDelete = profileManager.getProfiles().get(profileIndex);
                boolean wasFavorite = profileManager.isFavorite(ProfileManager.MODE_CUSTOM, profileIndex);

                profileManager.setCurrentProfileIndex(profileIndex);
                profileManager.deleteCurrentProfile();

                ProfileDeletionResult deletionResult = new ProfileDeletionResult(
                        profileToDelete, wasFavorite, !profileManager.hasProfiles());

                ProfileOperationResult result = ProfileOperationResult.success(
                        "Profil erfolgreich gelöscht", null, -1, deletionResult);

                mainHandler.post(() -> callback.onComplete(result));

            } catch (Exception e) {
                ProfileOperationResult result = ProfileOperationResult.error(
                        "Fehler beim Löschen des Profils", e);
                mainHandler.post(() -> callback.onComplete(result));
            }
        });
    }

    public void updateProfile(int profileIndex, CustomProfile updatedProfile, ProfileOperationCallback callback) {
        executorService.execute(() -> {
            try {
                if (profileIndex < 0 || profileIndex >= profileManager.getProfiles().size()) {
                    throw new IllegalArgumentException("Ungültiger Profil-Index");
                }

                profileManager.setCurrentProfileIndex(profileIndex);
                profileManager.updateCurrentProfile(updatedProfile);

                ProfileOperationResult result = ProfileOperationResult.success(
                        "Profil erfolgreich aktualisiert", updatedProfile, profileIndex);

                mainHandler.post(() -> callback.onComplete(result));

            } catch (Exception e) {
                ProfileOperationResult result = ProfileOperationResult.error(
                        "Fehler beim Aktualisieren des Profils", e);
                mainHandler.post(() -> callback.onComplete(result));
            }
        });
    }

    public void toggleFavorite(int mode, int customIndex, FavoriteToggleCallback callback) {
        executorService.execute(() -> {
            try {
                boolean wasAlreadyFavorite = profileManager.isFavorite(mode, customIndex);
                profileManager.toggleFavorite(mode, customIndex);
                boolean isNowFavorite = profileManager.isFavorite(mode, customIndex);

                FavoriteToggleResult result = new FavoriteToggleResult(
                        mode, customIndex, wasAlreadyFavorite, isNowFavorite);

                mainHandler.post(() -> callback.onToggleComplete(result));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Synchrone Getter für UI-Updates
    public List<CustomProfile> getProfiles() {
        return profileManager.getProfiles();
    }

    public boolean hasProfiles() {
        return profileManager.hasProfiles();
    }

    public CustomProfile getCurrentProfile() {
        return profileManager.getCurrentProfile();
    }

    public int getCurrentProfileIndex() {
        return profileManager.getCurrentProfileIndex();
    }

    public void setCurrentProfileIndex(int index) {
        profileManager.setCurrentProfileIndex(index);
    }

    // Getter für ProfileManager für Legacy-Kompatibilität
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // Data Classes
    public static class FavoriteModeResult {
        private final int mode;
        private final int customIndex;
        private final CustomProfile customProfile;

        private FavoriteModeResult(int mode, int customIndex, CustomProfile customProfile) {
            this.mode = mode;
            this.customIndex = customIndex;
            this.customProfile = customProfile;
        }

        public static FavoriteModeResult simple() {
            return new FavoriteModeResult(ProfileManager.MODE_SIMPLE, -1, null);
        }

        public static FavoriteModeResult extended() {
            return new FavoriteModeResult(ProfileManager.MODE_EXTENDED, -1, null);
        }

        public static FavoriteModeResult custom(int index, CustomProfile profile) {
            return new FavoriteModeResult(ProfileManager.MODE_CUSTOM, index, profile);
        }

        public int getMode() { return mode; }
        public int getCustomIndex() { return customIndex; }
        public CustomProfile getCustomProfile() { return customProfile; }
    }

    public static class ProfileCreationRequest {
        private final String name;
        private final int iconResId;
        private final Map<String, String> translations;

        public ProfileCreationRequest(String name, int iconResId, Map<String, String> translations) {
            this.name = name;
            this.iconResId = iconResId;
            this.translations = translations;
        }

        public String getName() { return name; }
        public int getIconResId() { return iconResId; }
        public Map<String, String> getTranslations() { return translations; }
    }

    public static class ProfileDeletionResult {
        private final CustomProfile deletedProfile;
        private final boolean wasFavorite;
        private final boolean wasLastProfile;

        public ProfileDeletionResult(CustomProfile deletedProfile, boolean wasFavorite, boolean wasLastProfile) {
            this.deletedProfile = deletedProfile;
            this.wasFavorite = wasFavorite;
            this.wasLastProfile = wasLastProfile;
        }

        public CustomProfile getDeletedProfile() { return deletedProfile; }
        public boolean wasFavorite() { return wasFavorite; }
        public boolean wasLastProfile() { return wasLastProfile; }
    }

    public static class FavoriteToggleResult {
        private final int mode;
        private final int customIndex;
        private final boolean wasAlreadyFavorite;
        private final boolean isNowFavorite;

        public FavoriteToggleResult(int mode, int customIndex, boolean wasAlreadyFavorite, boolean isNowFavorite) {
            this.mode = mode;
            this.customIndex = customIndex;
            this.wasAlreadyFavorite = wasAlreadyFavorite;
            this.isNowFavorite = isNowFavorite;
        }

        public int getMode() { return mode; }
        public int getCustomIndex() { return customIndex; }
        public boolean wasAlreadyFavorite() { return wasAlreadyFavorite; }
        public boolean isNowFavorite() { return isNowFavorite; }
    }

    // Callback Interfaces
    public interface FavoriteModeCallback {
        void onSuccess(FavoriteModeResult result);
        void onError(Exception e);
    }

    public interface ProfileOperationCallback {
        void onComplete(ProfileOperationResult result);
    }

    public interface FavoriteToggleCallback {
        void onToggleComplete(FavoriteToggleResult result);
        void onError(Exception e);
    }
}