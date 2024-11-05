package com.academy.creator_hub.service;
import com.academy.creator_hub.dto.SignupRequestDto;
import com.academy.creator_hub.entity.User;
import com.academy.creator_hub.entity.UserRoleEnum;
import com.academy.creator_hub.entity.UserStatus;
import com.academy.creator_hub.jwt.JwtUtil;
import com.academy.creator_hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void signup(SignupRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = passwordEncoder.encode(requestDto.getPassword());

        Optional<User> checkUser =  userRepository.findByUsername(username);

        if (checkUser.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자 입니다.");
        }
        if (requestDto.getInterests() == null || requestDto.getInterests().size() != 3) {
            throw new IllegalArgumentException("관심사는 3개를 선택해야 합니다.");
        }

        UserRoleEnum role = UserRoleEnum.USER;

        User user = new User(username, password, requestDto.getName(), role,
                UserStatus.NORMAL, "", requestDto.getInterests());

        userRepository.save(user);
    }

    public void logout(User user) {
        User finduser = findByUsername(user.getUsername());
        checkUserStatus(finduser);
        finduser.updateRefresh("");
    }

    public void withdraw(User user) {
        User finduser = findByUsername(user.getUsername());
        checkUserStatus(finduser);
        finduser.updateRefresh("");
        finduser.updateStatus(UserStatus.LEAVE);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다.")
        );
    }

    private void checkUserStatus(User user) {
        if (!user.isExist()) {
            throw new IllegalArgumentException("탈퇴한 유저 입니다.");
        }
    }
}
