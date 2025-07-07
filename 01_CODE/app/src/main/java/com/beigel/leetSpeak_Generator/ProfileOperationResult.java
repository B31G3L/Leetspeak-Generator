package com.beigel.leetSpeak_Generator;

public class ProfileOperationResult {
    private final boolean success;
    private final String message;
    private final Exception exception;
    private final CustomProfile profile;
    private final int profileIndex;
    private final ProfileRepository.ProfileDeletionResult deletionResult;

    private ProfileOperationResult(boolean success, String message, Exception exception,
                                   CustomProfile profile, int profileIndex,
                                   ProfileRepository.ProfileDeletionResult deletionResult) {
        this.success = success;
        this.message = message;
        this.exception = exception;
        this.profile = profile;
        this.profileIndex = profileIndex;
        this.deletionResult = deletionResult;
    }

    public static ProfileOperationResult success(String message, CustomProfile profile, int profileIndex) {
        return new ProfileOperationResult(true, message, null, profile, profileIndex, null);
    }

    public static ProfileOperationResult success(String message, CustomProfile profile, int profileIndex,
                                                 ProfileRepository.ProfileDeletionResult deletionResult) {
        return new ProfileOperationResult(true, message, null, profile, profileIndex, deletionResult);
    }

    public static ProfileOperationResult error(String message, Exception exception) {
        return new ProfileOperationResult(false, message, exception, null, -1, null);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Exception getException() { return exception; }
    public CustomProfile getProfile() { return profile; }
    public int getProfileIndex() { return profileIndex; }
    public ProfileRepository.ProfileDeletionResult getDeletionResult() { return deletionResult; }
}