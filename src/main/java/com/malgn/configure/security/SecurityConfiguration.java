package com.malgn.configure.security;

import com.malgn.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.Optional;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableJpaAuditing
public class SecurityConfiguration {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager를 사용하기 위한 설정
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 👇 로그인과 회원가입 경로는 인증 없이 접근 가능해야 합니다!
                        .requestMatchers("/api/members/join", "/api/members/login").permitAll()
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // JSON 로그인을 직접 구현했으므로 시큐리티 기본 폼/HTTP 기본 인증은 끔
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .logout(logout -> logout
                        .logoutUrl("/api/members/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(200);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\": \"Logout Success\"}");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    /**
     * JPA Auditing (@CreatedBy)을 위한 빈 등록
     * 이 설정이 있어야 엔티티의 createdBy 필드에 값이 자동으로 채워집니다.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // 1. 현재 보안 컨텍스트에서 인증 정보를 가져옵니다.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // 2. 인증 정보가 없거나, 인증되지 않았거나, 익명 사용자인 경우 "SYSTEM" 반환
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("SYSTEM");
            }

            // 3. CustomUserDetails를 사용 중이라면 저장된 실제 이름을 꺼내옵니다.
            if (auth.getPrincipal() instanceof CustomUserDetails) {
                return Optional.of(((CustomUserDetails) auth.getPrincipal()).getName());
            }

            // 4. 그 외의 경우(기본 User 객체 등)는 식별자(ID)를 반환합니다.
            return Optional.of(auth.getName());
        };
    }

    // 1. 401 Unauthorized (인증 안됨)
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"로그인이 필요한 서비스입니다.\", \"code\": 401}");
        };
    }

    // 2. 403 Forbidden (권한 없음)
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"message\": \"해당 리소스에 접근할 권한이 없습니다.\", \"code\": 403}");
        };
    }
}
