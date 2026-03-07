package com.malgn.service;

import com.malgn.domain.Member;
import com.malgn.domain.Role;
import com.malgn.dto.member.MemberJoinRequestDto;
import com.malgn.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 관리자 가입을 위한 임시 토큰 (실제로는 설정파일 등에서 관리)
    private final String ADMIN_TOKEN = "MALGN_ADMIN_SECRET";

    public Long join(MemberJoinRequestDto dto) {
        // 1. 중복 검사 실행
        validateDuplicateMember(dto);

        // 2. 권한 결정 로직
        Role userRole = Role.USER;
        if (dto.getRole() == Role.ADMIN) {
            if (dto.getAdminToken() == null || !dto.getAdminToken().equals(ADMIN_TOKEN)) {
                throw new IllegalStateException("관리자 암호가 일치하지 않아 가입이 불가능합니다.");
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
     * 중복 값 검사 로직 (ID, Email)
     */
    private void validateDuplicateMember(MemberJoinRequestDto dto) {
        if (memberRepository.existsByLoginId(dto.getLoginId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
    }
}
