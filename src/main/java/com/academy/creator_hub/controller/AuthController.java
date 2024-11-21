package com.academy.creator_hub.controller;

import com.academy.creator_hub.dto.SignupRequestDto;
import com.academy.creator_hub.security.UserDetailsImpl;
import com.academy.creator_hub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        System.out.println("Received request data: " + signupRequestDto);
        authService.signup(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        authService.logout(userDetails.getUser());
        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        authService.withdraw(userDetails.getUser());
        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
    }
}
