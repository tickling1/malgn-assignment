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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private MemberService memberService;

    private final String TEST_ADMIN_SECRET = "malgn_test_secret";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(memberService, "ADMIN_TOKEN", TEST_ADMIN_SECRET);
    }

    /**
     * 1. 회원가입 DTO 생성
     */
    private MemberJoinRequestDto createJoinDto(String loginId, String password, String email, String name, Role role, String adminToken) {
        MemberJoinRequestDto dto = new MemberJoinRequestDto();
        dto.setLoginId(loginId);
        dto.setPassword(password);
        dto.setEmail(email);
        dto.setName(name);
        dto.setRole(role);
        dto.setAdminToken(adminToken);
        return dto;
    }

    // 일반 유저 생성
    private MemberJoinRequestDto createDefaultUserDto() {
        return createJoinDto("user1", "pw123456", "user@test.com", "길동이", Role.USER, null);
    }

    // 관리자 생성 (컴파일 에러 해결 지점)
    private MemberJoinRequestDto createAdminDefaultDto() {
        // 기존에 Role.ADMIN이 중복되거나 순서가 틀렸던 부분을 수정했습니다.
        return createJoinDto("admin", "pw123456", "admin@test.com", "관리자", Role.ADMIN, TEST_ADMIN_SECRET);
    }

    /**
     * 2. 로그인 DTO 생성
     */
    private LoginRequestDto createLoginDto(String id, String pw) {
        LoginRequestDto dto = new LoginRequestDto();
        ReflectionTestUtils.setField(dto, "loginId", id);
        ReflectionTestUtils.setField(dto, "password", pw);
        return dto;
    }

    // --- 테스트 케이스 ---
    @Test
    @DisplayName("회원가입 성공: 일반 유저 가입 시 비밀번호 암호화 및 DB 저장 확인")
    void join_Success_User() {
        // given
        MemberJoinRequestDto dto = createDefaultUserDto();
        given(memberRepository.existsByLoginId(dto.getLoginId())).willReturn(false);
        given(memberRepository.existsByEmail(dto.getEmail())).willReturn(false);
        given(passwordEncoder.encode(dto.getPassword())).willReturn("encrypted_pw");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        // when
        Long savedId = memberService.join(dto);

        // then
        assertThat(savedId).isEqualTo(1L);
        verify(passwordEncoder).encode(dto.getPassword());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 성공: 관리자 토큰 일치 시 ADMIN 권한 부여")
    void join_Success_Admin() {
        // given
        MemberJoinRequestDto dto = createAdminDefaultDto();
        given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        memberService.join(dto);

        // then
        verify(memberRepository).save(argThat(member -> member.getRole() == Role.ADMIN));
    }

    @Test
    @DisplayName("회원가입 실패: 관리자 토큰 불일치 시 INVALID_ADMIN_TOKEN 예외")
    void join_Fail_InvalidAdminToken() {
        // given
        MemberJoinRequestDto dto = createJoinDto("admin1", "pw123", "admin@test.com", "관리자", Role.ADMIN, "wrong_token");

        // when & then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ADMIN_TOKEN);
    }

    @Test
    @DisplayName("회원가입 실패: 관리자 권한을 요청했으나 토큰이 누락된 경우")
    void join_Fail_AdminTokenMissing() {
        // given
        MemberJoinRequestDto dto = createJoinDto("admin2", "pw123", "admin2@test.com", "관리자", Role.ADMIN, null);

        // when & then
        assertThatThrownBy(() -> memberService.join(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ADMIN_TOKEN);
    }

    @Test
    @DisplayName("로그인 성공: 세션 내 SecurityContext 저장 확인")
    void login_Success() {
        // given
        LoginRequestDto dto = createLoginDto("user1", "pw123");
        MockHttpServletRequest request = new MockHttpServletRequest();

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getName()).willReturn("길동이");
        given(userDetails.getRole()).willReturn(String.valueOf(Role.USER));

        // when
        LoginResponseDto response = memberService.tryLogin(dto, request);

        // then
        assertThat(response.getName()).isEqualTo("길동이");
        assertThat(request.getSession().getAttribute("SPRING_SECURITY_CONTEXT")).isNotNull();
    }

    @Test
    @DisplayName("로그인 성공: 반환된 DTO에 사용자 이름과 권한이 정확히 담겨야 한다")
    void login_Success_ReturnDto() {
        // given
        LoginRequestDto dto = createLoginDto("user1", "pw123");
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        given(authenticationManager.authenticate(any())).willReturn(auth);
        given(auth.getPrincipal()).willReturn(userDetails);
        given(userDetails.getName()).willReturn("홍길동");
        given(userDetails.getRole()).willReturn(Role.USER.name());

        // when
        LoginResponseDto response = memberService.tryLogin(dto, new MockHttpServletRequest());

        // then
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo(Role.USER.name());
    }

    @Test
    @DisplayName("로그인 실패: 잘못된 비밀번호 입력 시 LOGIN_FAILED 예외")
    void login_Fail_BadCredentials() {
        // given
        LoginRequestDto dto = createLoginDto("user1", "wrong_pw");
        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("failed"));

        // when & then
        assertThatThrownBy(() -> memberService.tryLogin(dto, new MockHttpServletRequest()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);
    }
}