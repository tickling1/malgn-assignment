package com.malgn.service;

import com.malgn.domain.Member;
import com.malgn.domain.Role;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serializable;
import java.util.List;

@Getter
public class CustomUserDetails extends User implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Long id;     // DB PK 값
    private final String name; // 닉네임
    private final String role;

    public CustomUserDetails(Member member) {
        super(member.getLoginId(),
                member.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
        this.id = member.getId();
        this.name = member.getName();
        this.role = member.getRole().toString();
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }
}
