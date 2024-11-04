package com.academy.creator_hub.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignupRequestDto {

    @NotBlank(message = "사용자 Email은 필수 입력 사항입니다.")
    @Email(message = "정확한 이메일 주소를 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 사항입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "최소 8자 이상, 15자 이하이며 알파벳 대소문자(az, AZ), 숫자(0~9),특수문자로 구성되어야 합니다.")
    private String password;

    @NotBlank(message = "사용자의 이름을 입력하세요.")
    private String name;

}
