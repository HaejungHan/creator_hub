package com.academy.creator_hub.domain.auth.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.*;
import java.util.List;

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

    @NotNull(message = "관심사는 필수 선택 사항입니다.")
    @Size(min = 1, max = 3, message = "최소 1개이상 3개이하로 선택하세요.")
    private List<String> interests;
}
