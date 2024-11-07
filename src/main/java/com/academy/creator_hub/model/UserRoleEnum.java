package com.academy.creator_hub.model;

public enum UserRoleEnum {
    MANAGER(Authority.MANAGER),  // 매니저 사용자
    USER(Authority.USER);  // 일반 사용자

    private final String authority;

    UserRoleEnum(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return this.authority;
    }

    public static class Authority {
        public static final String MANAGER = "MANAGER";
        public static final String USER = "USER";
    }
}
