package com.malgn.dto.member;

import com.malgn.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청 정보")
public class MemberJoinRequestDto {

    @Schema(description = "로그인 아이디 (4~20자)", example = "user123")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    private String loginId;

    @Schema(description = "비밀번호 (8자 이상, 특수문자 권장)", example = "password123!")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "사용자 실명", example = "홍길동")
    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @Schema(description = "이메일 주소 (형식 체크)", example = "malgn@example.com")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "회원 권한 (USER: 일반 사용자, ADMIN: 관리자)",
            example = "USER",
            allowableValues = {"USER", "ADMIN"})
    private Role role;

    @Schema(description = "관리자 가입용 시크릿 토큰 (Role이 ADMIN인 경우 필수)", example = "MALGN_ADMIN_SECRET")
    private String adminToken;
}