package com.academy.creator_hub.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "users")
public class User extends Timestamped {

    private String username;
    private String name;
    private String password;
    private UserRoleEnum role;
    private UserStatus userStatus;
    private String refreshToken;

    public void updateRefresh(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public boolean isExist() {
        return this.userStatus == UserStatus.NORMAL;
    }

    public User (
            String username, String password, UserRoleEnum role, UserStatus userStatus,
            String refreshToken) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userStatus = userStatus;
        this.refreshToken = refreshToken;
    }
}
