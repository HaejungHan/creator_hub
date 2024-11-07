package com.academy.creator_hub.model;

public enum Interest {
    SPORTS("Sports"),
    MUSIC("Music"),
    TECH("Tech"),
    ART("Art"),
    TRAVEL("Travel");

    private final String displayName;

    Interest(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
