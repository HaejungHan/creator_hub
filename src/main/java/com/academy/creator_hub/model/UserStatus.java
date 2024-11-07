package com.academy.creator_hub.model;

public enum UserStatus {
    NORMAL(Authority.NORMAL),  // 정상 유저
    LEAVE(Authority.LEAVE);  // 탈퇴 유저

    private final String userStatus;

    UserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserStatus() {
        return this.userStatus;
    }

    public static class Authority {
        public static final String NORMAL = "NORMAL";
        public static final String LEAVE = "LEAVE";
    }
}
