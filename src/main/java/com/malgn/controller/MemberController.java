package com.malgn.controller;

import com.malgn.dto.login.LoginRequestDto;
import com.malgn.dto.login.LoginResponseDto;
import com.malgn.dto.member.MemberJoinRequestDto;
import com.malgn.service.MemberService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입 API (JSON 요청을 처리합니다)
     */
    @PostMapping("/join")
    public ResponseEntity<Long> join(@Valid @RequestBody MemberJoinRequestDto dto) {
        Long memberId = memberService.join(dto);
        return ResponseEntity.ok(memberId);
    }
    /**
     * 로그인 API (JSON 요청을 처리합니다)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto, HttpServletRequest request) {
        LoginResponseDto responseDto = memberService.tryLogin(dto);

        // 세션 동기화
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        return ResponseEntity.ok(responseDto);
    }
}
