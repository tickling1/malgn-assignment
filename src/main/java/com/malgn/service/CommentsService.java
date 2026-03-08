package com.malgn.service;

import com.malgn.configure.security.CustomUserDetails;
import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import com.malgn.domain.Member;
import com.malgn.dto.comments.CommentRequestDto;
import com.malgn.exception.BusinessException;
import com.malgn.exception.ErrorCode;
import com.malgn.repository.CommentsRepository;
import com.malgn.repository.ContentsRepository;
import com.malgn.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final ContentsRepository contentsRepository;
    private final MemberRepository memberRepository;


    /**
     * 1. 댓글 및 대댓글 등록
     * CustomUserDetails를 활용하여 작성자 정보를 주입합니다.
     */
    @Transactional
    public Long createComment(CommentRequestDto dto, CustomUserDetails user) {
        // [검증 1] 내용 유효성
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Contents content = contentsRepository.findById(dto.getContentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        Member member = memberRepository.getReferenceById(user.getId());

        Comments parentComment = null;
        if (dto.getParentId() != null) {
            parentComment = commentsRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

            // [검증 2] 정합성 체크
            if (!Objects.equals(parentComment.getParentContent().getId(), content.getId())) {
                throw new BusinessException(ErrorCode.COMMENT_CONTENT_MISMATCH);
            }

            // [검증 3] 계층 제한 (Depth 1까지만 허용)
            if (parentComment.getParentComment() != null) {
                throw new BusinessException(ErrorCode.OVER_COMMENT_DEPTH);
            }
        }

        Comments comment = Comments.builder()
                .content(dto.getContent())
                .parentContent(content)
                .parentComment(parentComment)
                .member(member)
                .build();

        return commentsRepository.save(comment).getId();
    }
    /**
     * 2. 댓글 수정
     */
    public void updateComment(Long id, String newContent, CustomUserDetails user) {
        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // CustomUserDetails의 id와 엔티티의 member id를 비교
        validateAuthorOrAdmin(comment.getMember().getId(), user);

        comment.update(newContent);
    }

    /**
     * 3. 댓글 삭제
     */
    public void deleteComment(Long id, CustomUserDetails user) {
        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(comment.getMember().getId(), user);

        commentsRepository.delete(comment);
    }

    /**
     * 권한 검증 공통 메서드
     */
    private void validateAuthorOrAdmin(Long authorId, CustomUserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        // 고유 식별자(PK) 기반 비교로 보안 강화
        if (!isAdmin && !Objects.equals(authorId, user.getId())) {
            throw new BusinessException(ErrorCode.NOT_AUTHOR);
        }
    }
}