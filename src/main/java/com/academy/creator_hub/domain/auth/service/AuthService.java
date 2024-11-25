package com.academy.creator_hub.domain.auth.service;
import com.academy.creator_hub.domain.auth.dto.SignupRequestDto;
import com.academy.creator_hub.domain.auth.model.Interest;
import com.academy.creator_hub.domain.auth.model.User;
import com.academy.creator_hub.domain.auth.model.UserRoleEnum;
import com.academy.creator_hub.domain.auth.model.UserStatus;
import com.academy.creator_hub.jwt.JwtUtil;
import com.academy.creator_hub.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        String username = signupRequestDto.getUsername();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());

        Optional<User> checkUser = userRepository.findByUsername(username);

        if (checkUser.isPresent()) {
            // 중복된 사용자일 경우
            throw new IllegalArgumentException("중복된 사용자 입니다.");
        }

        if (signupRequestDto.getInterests() == null || signupRequestDto.getInterests().size() != 3) {
            throw new IllegalArgumentException("관심사는 3개를 선택해야 합니다.");
        }

        List<String> interestDisplayNames = signupRequestDto.getInterests().stream()
                .map(interest -> interest.getDisplayName())
                .collect(Collectors.toList());

        UserRoleEnum role = UserRoleEnum.USER;

        User user = new User(username, password, role, UserStatus.NORMAL, "", interestDisplayNames);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
