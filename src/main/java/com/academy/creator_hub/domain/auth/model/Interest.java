package com.academy.creator_hub.domain.auth.model;

public enum Interest {
    SPORTS("스포츠"),
    MUSIC("음악"),
    STUDY("공부"),
    ART("예술"),
    TRAVEL("여행"),
    MOVIES("영화"),
    FOOD("음식"),
    READING("독서"),
    GAMING("게임"),
    FASHION("패션"),
    SHOPPING("쇼핑"),
    IT_DEV("개발");

    private final String displayName;

    Interest(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
