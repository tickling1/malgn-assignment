package com.malgn.service;

import com.malgn.configure.security.CustomUserDetails;
import com.malgn.domain.Member;
import com.malgn.domain.Role;
import com.malgn.dto.login.LoginRequestDto;
import com.malgn.dto.login.LoginResponseDto;
import com.malgn.dto.member.MemberJoinRequestDto;
import com.malgn.exception.BusinessException;
import com.malgn.exception.ErrorCode;
import com.malgn.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * 관리자 권한 부여를 위한 보안 토큰입니다.
     * 현재는 application.yml 파일에서 관리하고 있으며
     * 운영 환경에서는 외부 환경 변수, aws secret manager를 통해 주입받아야 합니다.
     */
    @Value("${malgn.admin.secret}")
    private String ADMIN_TOKEN;

    /**
     * 회원 가입 기능
     */
    public Long join(MemberJoinRequestDto dto) {
        // 1. 중복 검사 실행
        validateDuplicateMember(dto);

        // 2. 권한 결정 로직
        Role userRole = Role.USER;
        if (dto.getRole() == Role.ADMIN) {
            if (dto.getAdminToken() == null || !dto.getAdminToken().equals(ADMIN_TOKEN)) {
                throw new BusinessException(ErrorCode.INVALID_ADMIN_TOKEN);
            }
            userRole = Role.ADMIN;
        }

        // 3. 엔티티 생성 및 저장
        Member member = Member.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .name(dto.getName())
                .role(userRole)
                .build();

        return memberRepository.save(member).getId();
    }

    /**
     * 세션 로그인 기능
     */
    public LoginResponseDto tryLogin(LoginRequestDto dto, HttpServletRequest request) { // HttpServletRequest 추가
        try {
            // 시큐리티 인증 시도
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword());

            Authentication authentication = authenticationManager.authenticate(token);

            //  인증 성공 시 SecurityContext 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션에 SecurityContext 저장
            HttpSession session = request.getSession(true); // 세션이 없으면 새로 생성
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            // 사용자 정보 추출 및 반환
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return LoginResponseDto.of(userDetails.getName(), userDetails.getRole());

        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }

    /**
     * 중복 값 검사 로직 (ID, Email)
     */
    @Transactional(readOnly = true)
    private void validateDuplicateMember(MemberJoinRequestDto dto) {
        if (memberRepository.existsByLoginId(dto.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
