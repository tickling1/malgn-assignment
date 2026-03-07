package com.malgn.service;

import com.malgn.domain.Member;
import com.malgn.domain.Role;
import com.malgn.dto.login.LoginRequestDto;
import com.malgn.dto.login.LoginResponseDto;
import com.malgn.dto.member.MemberJoinRequestDto;
import com.malgn.exception.BusinessException;
import com.malgn.exception.ErrorCode;
import com.malgn.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // 관리자 가입을 위한 임시 토큰 (실제로는 설정파일 등에서 관리)
    private final String ADMIN_TOKEN = "MALGN_ADMIN_SECRET";

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
     * 로그인 기능
     */
    public LoginResponseDto tryLogin(LoginRequestDto dto) {
        // 1. 아이디 존재 여부 먼저 확인
        memberRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        try {
            // 2. 시큐리티 인증 시도
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(dto.getLoginId(), dto.getPassword());

            Authentication authentication = authenticationManager.authenticate(token);

            // 3. 인증 성공 시 SecurityContext 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. 인증 객체에서 사용자 정보(CustomUserDetails) 추출
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 5. 로그인 결과 DTO 반환
            return LoginResponseDto.of(
                    userDetails.getName(),
                    userDetails.getRole()
            );

        } catch (BadCredentialsException e) {
            // 비밀번호 불일치 시
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
