package com.malgn.repository;

import com.malgn.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 로그인 ID로 회원 찾기
     * 시큐리티에서 loadUserByUsername을 구현할 때 필수적으로 사용됩니다.
     */
    Optional<Member> findByLoginId(String loginId);

    /**
     * 중복 가입 방지를 위한 체크용
     */
    boolean existsByLoginId(String loginId);
    // 이메일로 중복 체크를 하기 위해 추가
    boolean existsByEmail(String email);

}
