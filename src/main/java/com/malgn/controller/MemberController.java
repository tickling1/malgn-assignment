package com.malgn.controller;

import com.malgn.dto.login.LoginRequestDto;
import com.malgn.dto.login.LoginResponseDto;
import com.malgn.dto.member.MemberJoinRequestDto;
import com.malgn.exception.ErrorResponse;
import com.malgn.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 가입 및 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입 API
     * 현재는 ADMIN_TOKEN가 application.yml 파일에서 관리하고 있으며
     * 운영 환경에서는 외부 환경 변수, aws secret manager를 통해 주입받아야 합니다.
     */
    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 관리자(ADMIN) 권한 가입 시에는 유효한 '관리자 가입 토큰'이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),

            // 1. 403 에러 (관리자 토큰 불일치)
            @ApiResponse(
                    responseCode = "403",
                    description = "관리자 토큰 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "관리자 토큰 오류 예시",
                                    value = """
                {
                  "timestamp": "2026-03-07T23:10:00",
                  "status": 403,
                  "code": "INVALID_ADMIN_TOKEN",
                  "message": "관리자 인증 토큰이 유효하지 않습니다.",
                  "path": "/api/members/join"
                }
                """
                            )
                    )
            ),

            // 2. 400 에러 (입력값 검증 실패)
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "검증 실패 예시",
                                    value = """
                {
                  "timestamp": "2026-03-07T23:15:00",
                  "status": 400,
                  "code": "INVALID_INPUT_VALUE",
                  "message": "아이디는 4~20자 사이여야 합니다.",
                  "path": "/api/members/join"
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/join")
    public ResponseEntity<Long> join(@Valid @RequestBody MemberJoinRequestDto dto) {
        Long memberId = memberService.join(dto);
        return ResponseEntity.ok(memberId);
    }


    /**
     * 로그인 API
     */
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 확인하고 세션을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 및 세션 생성",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 (아이디 또는 비밀번호 불일치)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 실패 예시",
                                    value = """
                {
                  "timestamp": "2026-03-07T23:20:00",
                  "status": 401,
                  "code": "LOGIN_FAILED",
                  "message": "아이디 또는 비밀번호가 일치하지 않습니다.",
                  "path": "/api/members/login"
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto, HttpServletRequest request) {
        LoginResponseDto responseDto = memberService.tryLogin(dto, request);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        return ResponseEntity.ok(responseDto);
    }
}
