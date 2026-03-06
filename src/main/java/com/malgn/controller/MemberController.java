package com.malgn.controller;

import com.malgn.dto.login.LoginRequestDto;
import com.malgn.dto.member.MemberJoinRequestDto;
import com.malgn.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입 API (JSON 요청을 처리합니다)
     */
    @PostMapping("/join")
    public ResponseEntity<Long> join(@Valid @RequestBody MemberJoinRequestDto dto) {
        // MemberService의 join 로직을 호출합니다 (중복 검사, 관리자 토큰 검사 포함)
        Long memberId = memberService.join(dto);
        return ResponseEntity.ok(memberId);
    }

    /**
     * 로그인 API (JSON 요청을 처리합니다)
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto dto, HttpServletRequest request) {
        // 1. 아이디와 비밀번호로 인증 토큰 생성
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword());

        // 2. 실제 검증 (CustomUserDetailsService가 호출됨)
        Authentication authentication = authenticationManager.authenticate(token);

        // 3. SecurityContext에 인증 정보 저장 (세션 생성)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. 세션 유지 설정
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return ResponseEntity.ok("로그인 성공! 환영합니다.");
    }
}
