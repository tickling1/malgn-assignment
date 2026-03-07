package com.malgn.service;

import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import com.malgn.dto.comments.CommentRequestDto;
import com.malgn.exception.BusinessException;
import com.malgn.exception.ErrorCode;
import com.malgn.repository.CommentsRepository;
import com.malgn.repository.ContentsRepository;
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

    /**
     * 1. 댓글 및 대댓글 등록
     */
    public Long createComment(CommentRequestDto dto) {
        Contents content = contentsRepository.findById(dto.getContentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        Comments parentComment = null;
        if (dto.getParentId() != null) {
            parentComment = commentsRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        Comments comment = Comments.builder()
                .content(dto.getContent())
                .parentContent(content)
                .parentComment(parentComment)
                .build();

        return commentsRepository.save(comment).getId();
    }

    /**
     * 2. 댓글 수정
     */
    public void updateComment(Long id, String newContent, CustomUserDetails user) {
        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(comment.getCreatedBy(), user);

        comment.update(newContent);
    }

    /**
     * 3. 댓글 삭제
     */
    public void deleteComment(Long id, CustomUserDetails user) {
        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(comment.getCreatedBy(), user);

        commentsRepository.delete(comment);
    }

    /**
     * 권한 검증 공통 메서드
     */
    private void validateAuthorOrAdmin(String authorName, CustomUserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        // Objects.equals를 사용하여 authorName이 null인 경우에도 안전하게 비교
        if (!isAdmin && !Objects.equals(authorName, user.getName())) {
            throw new BusinessException(ErrorCode.NOT_AUTHOR);
        }
    }
}