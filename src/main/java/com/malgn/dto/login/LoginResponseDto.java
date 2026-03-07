package com.malgn.dto.login;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponseDto {

    private String name;
    private String role;
    private String message;

    @Builder
    public LoginResponseDto(String loginId, String name, String role, String message) {
        this.name = name;
        this.role = role;
        this.message = message;
    }

    // 서비스나 컨트롤러에서 정적 팩토리 메서드로 편하게 생성할 수도 있습니다.
    public static LoginResponseDto of(String name, String role) {
        return LoginResponseDto.builder()
                .name(name)
                .role(role)
                .message("로그인 성공! 환영합니다.")
                .build();
    }
}
