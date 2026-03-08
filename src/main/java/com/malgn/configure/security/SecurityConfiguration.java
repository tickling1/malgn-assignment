package com.malgn.configure.security;

import com.malgn.exception.ErrorResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableJpaAuditing
public class SecurityConfiguration {

    private final ObjectMapper objectMapper;

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
                        .requestMatchers("/api/members/join/**", "/api/members/login/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**","/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .requestCache(cache -> cache.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .logout(logout -> logout
                        .logoutUrl("/api/members/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            Map<String, Object> logoutResponse = new LinkedHashMap<>();
                            logoutResponse.put("timestamp", LocalDateTime.now().toString());
                            logoutResponse.put("status", HttpServletResponse.SC_OK);
                            logoutResponse.put("message", "로그아웃이 성공적으로 완료되었습니다.");

                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(objectMapper.writeValueAsString(logoutResponse));
                        })
                        .invalidateHttpSession(true) // 서버 측 세션 무효화 (DB 데이터 삭제 유도)
                        .clearAuthentication(true)   // 인증 정보 삭제
                        .deleteCookies("JSESSIONID", "SESSION")
                );

        return http.build();
    }

    /**
     * JPA Auditing (@CreatedBy, @LastModifiedBy)을 위한 빈 등록
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // 현재 보안 컨텍스트에서 인증 정보를 가져옵니다.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("SYSTEM");
            }

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
            ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "C004",
                    "로그인이 필요한 서비스입니다.",
                    request.getRequestURI()
            );

            sendError(response, errorResponse);
        };
    }

    // 2. 403 Forbidden (권한 없음)
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ErrorResponse errorResponse = new ErrorResponse(
                    LocalDateTime.now(),
                    HttpServletResponse.SC_FORBIDDEN,
                    "C003", // 권한 관련 공통 코드
                    "해당 리소스에 접근할 권한이 없습니다.",
                    request.getRequestURI()
            );

            sendError(response, errorResponse);
        };
    }

    // 응답 전송 공통 메서드
    private void sendError(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorResponse.status());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
