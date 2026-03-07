package com.malgn.service;

import com.malgn.domain.Member;
import com.malgn.domain.Role;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class CustomUserDetails extends User {

    private final Long id;     // DB PK 값
    private final String name; // 우리가 다른 곳에서 쓰고 싶은 '진짜 이름'
    private final String role;

    public CustomUserDetails(Member member) {
        // 부모(User)에게는 식별자인 loginId를 전달 (시큐리티가 ID 검증 시 사용)
        super(member.getLoginId(),
                member.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));

        // 추가로 필요한 id, name을 저장
        this.id = member.getId();
        this.name = member.getName();
        this.role = member.getRole().toString();
    }
}
