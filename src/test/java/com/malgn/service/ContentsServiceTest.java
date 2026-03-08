package com.malgn.service;

import com.malgn.configure.security.CustomUserDetails;
import com.malgn.domain.Contents;
import com.malgn.domain.Member;
import com.malgn.domain.Role;
import com.malgn.dto.contents.ContentDetailResponseDto;
import com.malgn.dto.contents.ContentRequestDto;
import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.exception.BusinessException;
import com.malgn.exception.ErrorCode;
import com.malgn.repository.ContentsRepository;
import com.malgn.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentsServiceTest {

    @Mock
    private ContentsRepository contentsRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ContentsService contentsService;

    private Contents content;
    private Member author;

    @BeforeEach
    void setUp() {
        author = mock(Member.class);
        content = mock(Contents.class);
    }

    // CustomUserDetails 생성 헬퍼
    private CustomUserDetails createUserDetails(Long id, Role role) {
        Member member = mock(Member.class);
        lenient().when(member.getId()).thenReturn(id);
        lenient().when(member.getRole()).thenReturn(role);
        lenient().when(member.getLoginId()).thenReturn("user" + id);
        lenient().when(member.getPassword()).thenReturn("password");
        return new CustomUserDetails(member);
    }

    @Test
    @DisplayName("콘텐츠 생성 성공: 작성자 정보가 정상적으로 매핑되어 저장된다")
    void createContent_Success() {
        // given
        ContentRequestDto dto = new ContentRequestDto("제목", "내용");
        CustomUserDetails user = createUserDetails(1L, Role.USER);

        // 작성자 프록시 객체 반환 설정
        given(memberRepository.getReferenceById(user.getId())).willReturn(author);
        given(contentsRepository.save(any(Contents.class))).willAnswer(inv -> {
            Contents c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 100L);
            return c;
        });

        // when
        Long savedId = contentsService.createContent(dto, user);

        // then
        assertThat(savedId).isEqualTo(100L);

        // 검증: 제목과 작성자가 제대로 들어갔는지 확인
        verify(contentsRepository).save(argThat(c ->
                c.getTitle().equals("제목") &&
                        c.getAuthor().equals(author)
        ));
    }

    @Test
    @DisplayName("콘텐츠 상세 조회: 조회수가 1 증가하고 상세 정보를 반환한다")
    void getContentDetail_Success() {
        // given
        Long contentId = 1L;
        Contents realContent = Contents.builder()
                .title("제목")
                .description("내용")
                .build();
        // 초기 조회수는 0이라고 가정
        given(contentsRepository.findById(contentId)).willReturn(Optional.of(realContent));

        // when
        ContentDetailResponseDto response = contentsService.getContentDetail(contentId);

        // then
        assertThat(realContent.getViewCount()).isEqualTo(1); //Dirty Checking 대상 확인
        assertThat(response.getTitle()).isEqualTo("제목");
        verify(contentsRepository).findById(contentId);
    }

    @Test
    @DisplayName("콘텐츠 수정 실패: 작성자가 아닌 유저가 수정을 시도하면 NOT_AUTHOR 예외 발생")
    void updateContent_Fail_NotAuthor() {
        // given
        Long contentId = 1L;
        Long authorId = 10L;
        Long strangerId = 20L;

        given(contentsRepository.findById(contentId)).willReturn(Optional.of(content));
        given(content.getAuthor()).willReturn(author);
        given(author.getId()).willReturn(authorId);

        CustomUserDetails stranger = createUserDetails(strangerId, Role.USER);
        ContentRequestDto updateDto = new ContentRequestDto("수정 제목", "수정 내용");

        // when & then
        assertThatThrownBy(() -> contentsService.updateContent(contentId, updateDto, stranger))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_AUTHOR);
    }

    @Test
    @DisplayName("콘텐츠 삭제 성공: 관리자 권한이면 타인의 글도 삭제 가능하다")
    void deleteContent_Admin_Success() {
        // given
        Long contentId = 1L;
        given(contentsRepository.findById(contentId)).willReturn(Optional.of(content));
        given(content.getAuthor()).willReturn(author);
        given(author.getId()).willReturn(10L); // 원작자 10L

        CustomUserDetails admin = createUserDetails(999L, Role.ADMIN); // 관리자 999L

        // when & then
        assertDoesNotThrow(() -> contentsService.deleteContent(contentId, admin));
        verify(contentsRepository).delete(content);
    }

    @Test
    @DisplayName("콘텐츠 수정 성공: 작성자 본인은 내용을 수정할 수 있다")
    void updateContent_Success() {
        // given
        Long contentId = 1L;
        Long memberId = 10L;
        ContentRequestDto updateDto = new ContentRequestDto("수정된 제목", "수정된 내용");
        CustomUserDetails user = createUserDetails(memberId, Role.USER);

        given(contentsRepository.findById(contentId)).willReturn(Optional.of(content));
        given(content.getAuthor()).willReturn(author);
        given(author.getId()).willReturn(memberId); // 작성자 ID 일치

        // when
        assertDoesNotThrow(() -> contentsService.updateContent(contentId, updateDto, user));

        // then
        verify(content).update("수정된 제목", "수정된 내용");
    }

    @Test
    @DisplayName("콘텐츠 수정 실패: 존재하지 않는 콘텐츠를 수정하려 하면 RESOURCE_NOT_FOUND 발생")
    void updateContent_NotFound_Fail() {
        // given
        Long contentId = 999L;
        CustomUserDetails user = createUserDetails(1L, Role.USER);
        given(contentsRepository.findById(contentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contentsService.updateContent(contentId, new ContentRequestDto("제목", "내용"), user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("콘텐츠 삭제 실패: 작성자가 아닌 유저(일반유저)가 삭제를 시도하면 NOT_AUTHOR 발생")
    void deleteContent_NotAuthor_Fail() {
        // given
        Long contentId = 1L;
        Long authorId = 10L;
        Long strangerId = 20L;

        given(contentsRepository.findById(contentId)).willReturn(Optional.of(content));
        given(content.getAuthor()).willReturn(author);
        given(author.getId()).willReturn(authorId);

        CustomUserDetails stranger = createUserDetails(strangerId, Role.USER);

        // when & then
        assertThatThrownBy(() -> contentsService.deleteContent(contentId, stranger))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_AUTHOR);

        // 실제 삭제 메서드가 호출되지 않았는지 확인
        verify(contentsRepository, never()).delete(any(Contents.class));
    }

    @Test
    @DisplayName("콘텐츠 목록 조회: 페이징된 결과를 반환한다")
    void getContentsList_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contents> contentPage = new PageImpl<>(List.of(content));
        given(contentsRepository.findAll(pageable)).willReturn(contentPage);

        // when
        Page<ContentResponseDto> result = contentsService.getContentsList(pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(contentsRepository).findAll(pageable);
    }
}