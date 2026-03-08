package com.malgn.service;

import com.malgn.configure.security.CustomUserDetails;
import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import com.malgn.domain.Member;
import com.malgn.domain.Role;
import com.malgn.dto.comments.CommentRequestDto;
import com.malgn.exception.BusinessException;
import com.malgn.exception.ErrorCode;
import com.malgn.repository.CommentsRepository;
import com.malgn.repository.ContentsRepository;
import com.malgn.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentsServiceTest {

    @Mock private CommentsRepository commentsRepository;
    @Mock private ContentsRepository contentsRepository;
    @Mock private MemberRepository memberRepository; // 추가됨
    @InjectMocks private CommentsService commentsService;

    private Contents content;
    private Comments parentComment;
    private Member authorMember;

    @BeforeEach
    void setUp() {
        content = mock(Contents.class);
        parentComment = mock(Comments.class);
        authorMember = mock(Member.class);
    }

    // CustomUserDetails 생성을 위한 헬퍼 메서드
    private CustomUserDetails createUserDetails(Long id, String loginId, Role role) {
        Member member = mock(Member.class);
        lenient().when(member.getId()).thenReturn(id);
        lenient().when(member.getLoginId()).thenReturn(loginId);
        lenient().when(member.getPassword()).thenReturn("password");
        lenient().when(member.getRole()).thenReturn(role);
        lenient().when(member.getName()).thenReturn("사용자" + id);
        return new CustomUserDetails(member);
    }

    @Test
    @DisplayName("댓글 등록: Member 연관관계가 정상적으로 맺어지며 저장된다")
    void createComment_Success() {
        // given
        Long userId = 1L;
        CustomUserDetails user = createUserDetails(userId, "user1", Role.USER);
        CommentRequestDto dto = new CommentRequestDto(10L, null, "댓글 내용");

        given(contentsRepository.findById(10L)).willReturn(Optional.of(content));
        // getReferenceById는 프록시를 반환하므로 authorMember를 반환하도록 설정
        given(memberRepository.getReferenceById(userId)).willReturn(authorMember);

        given(commentsRepository.save(any(Comments.class))).willAnswer(inv -> {
            Comments c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 100L);
            return c;
        });

        // when
        Long savedId = commentsService.createComment(dto, user);

        // then
        assertThat(savedId).isEqualTo(100L);
        verify(commentsRepository).save(argThat(c -> c.getMember() != null));
    }

    @Test
    @DisplayName("댓글 수정 실패: 작성자 ID와 로그인 유저 ID가 다르면 NOT_AUTHOR 예외 발생")
    void updateComment_Fail_IdMismatch() {
        // given
        Long commentId = 50L;
        Long authorId = 100L;
        Long otherUserId = 200L;

        Comments comment = mock(Comments.class);
        given(commentsRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(comment.getMember()).willReturn(authorMember);
        given(authorMember.getId()).willReturn(authorId); // 실제 작성자 ID: 100

        CustomUserDetails otherUser = createUserDetails(otherUserId, "other", Role.USER); // 로그인 유저 ID: 200

        // when & then
        assertThatThrownBy(() -> commentsService.updateComment(commentId, "수정", otherUser))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_AUTHOR);
    }

    @Test
    @DisplayName("관리자 수정 성공: ID가 달라도 관리자 권한(ROLE_ADMIN)이면 수정 가능")
    void updateComment_Admin_Success() {
        // given
        Long commentId = 50L;
        Comments comment = mock(Comments.class);
        given(commentsRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(comment.getMember()).willReturn(authorMember);
        given(authorMember.getId()).willReturn(100L); // 작성자 ID: 100

        CustomUserDetails adminUser = createUserDetails(999L, "admin", Role.ADMIN); // 관리자 ID: 999

        // when & then
        assertDoesNotThrow(() -> commentsService.updateComment(commentId, "관리자 수정", adminUser));
        verify(comment).update("관리자 수정");
    }

    @Test
    @DisplayName("대댓글 등록 실패: 부모 댓글이 속한 게시글 ID와 요청 게시글 ID가 다르면 예외가 발생한다")
    void createComment_InconsistentContentId_Fail() {
        // given
        Long contentId = 1L;
        Long otherContentId = 2L;
        Long parentCommentId = 10L;

        CommentRequestDto dto = new CommentRequestDto(contentId, parentCommentId, "대댓글");
        CustomUserDetails user = createUserDetails(1L, "user", Role.USER);

        given(contentsRepository.findById(contentId)).willReturn(Optional.of(content));
        given(commentsRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));

        Contents otherContent = mock(Contents.class);
        given(parentComment.getParentContent()).willReturn(otherContent);
        given(otherContent.getId()).willReturn(otherContentId);

        // when & then
        assertThatThrownBy(() -> commentsService.createComment(dto, user))
                .isInstanceOf(BusinessException.class)
                // [수정] INVALID_INPUT_VALUE -> COMMENT_CONTENT_MISMATCH
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_CONTENT_MISMATCH);
    }

    @Test
    @DisplayName("대댓글 등록 실패: 이미 대댓글인 댓글을 부모로 설정할 수 없다")
    void createComment_ExceedDepth_Fail() {
        // given
        Long contentId = 1L;
        Long parentId = 10L;
        CommentRequestDto dto = new CommentRequestDto(contentId, parentId, "3단계 댓글 시도");
        CustomUserDetails user = createUserDetails(1L, "user", Role.USER);

        given(contentsRepository.findById(contentId)).willReturn(Optional.of(content));
        given(commentsRepository.findById(parentId)).willReturn(Optional.of(parentComment));

        // [추가] NPE 방지: 부모 댓글이 속한 게시글 정보가 있어야 비교 로직을 통과하거나 시도할 수 있음
        given(parentComment.getParentContent()).willReturn(content);
        given(content.getId()).willReturn(contentId);

        // 부모 댓글이 이미 누군가(GrandParent)를 부모로 가지고 있는 상태 (Depth 1 초과 상황)
        given(parentComment.getParentComment()).willReturn(mock(Comments.class));

        // when & then
        assertThatThrownBy(() -> commentsService.createComment(dto, user))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OVER_COMMENT_DEPTH);
    }

    @Test
    @DisplayName("댓글 삭제 실패: 관리자 권한이 만료되었거나 비정상적인 접근인 경우")
    void deleteComment_InvalidRole_Fail() {
        // given
        Long commentId = 1L;
        Comments comment = mock(Comments.class);
        given(commentsRepository.findById(commentId)).willReturn(Optional.of(comment));

        // 권한이 없는 유저가 관리자인 척 시도
        CustomUserDetails fakeAdmin = mock(CustomUserDetails.class);
        given(fakeAdmin.getAuthorities()).willReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_USER")));
        given(fakeAdmin.getId()).willReturn(999L);

        given(comment.getMember()).willReturn(mock(Member.class));
        given(comment.getMember().getId()).willReturn(1L); // 실제 작성자는 1L

        // when & then
        assertThatThrownBy(() -> commentsService.deleteComment(commentId, fakeAdmin))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_AUTHOR);
    }
}