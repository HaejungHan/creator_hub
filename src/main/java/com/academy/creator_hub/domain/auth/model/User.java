package com.academy.creator_hub.domain.auth.model;

import com.academy.creator_hub.common.Timestamped;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@NoArgsConstructor
@Document(collection = "users")
public class User extends Timestamped {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String password;
    private UserRoleEnum role;
    private UserStatus userStatus;
    private String refreshToken;
    private List<String> Interests;

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
            String refreshToken, List<String> interests) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userStatus = userStatus;
        this.refreshToken = refreshToken;
        this.Interests = interests;
    }
}
