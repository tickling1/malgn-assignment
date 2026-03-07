package com.malgn.service;

import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import com.malgn.dto.comments.CommentRequestDto;
import com.malgn.repository.CommentsRepository;
import com.malgn.repository.ContentsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final ContentsRepository contentsRepository;

    /**
     * 1. 댓글 및 대댓글 등록
     * AuditorAware 덕분에 작성자(createdBy)는 자동 저장됩니다.
     */
    public Long createComment(CommentRequestDto dto) {
        Contents content = contentsRepository.findById(dto.getContentId())
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        Comments parentComment = null;
        if (dto.getParentId() != null) {
            parentComment = commentsRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글이 존재하지 않습니다."));
        }

        Comments comment = Comments.builder()
                .content(dto.getContent())
                .parentContent(content)
                .parentComment(parentComment)
                .build();

        return commentsRepository.save(comment).getId();
    }

    /**
     * 2. 댓글 수정 (작성자/관리자만)
     */
    public void updateComment(Long id, String newContent, CustomUserDetails user) {
        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        // 권한 검증 호출
        validateAuthorOrAdmin(comment.getCreatedBy(), user);

        comment.update(newContent);
    }

    /**
     * 3. 댓글 삭제 (작성자/관리자만)
     */
    public void deleteComment(Long id, CustomUserDetails user) {
        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        // 권한 검증 호출
        validateAuthorOrAdmin(comment.getCreatedBy(), user);

        commentsRepository.delete(comment);
    }

    /**
     * 권한 검증 공통 메서드
     */
    private void validateAuthorOrAdmin(String authorName, CustomUserDetails user) {
        // ROLE_ADMIN 권한이 있는지 확인
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 관리자가 아니고, 작성자 이름(createdBy)이 현재 로그인 유저의 이름과 다르면 예외 발생
        if (!isAdmin && !authorName.equals(user.getName())) {
            throw new IllegalStateException("해당 댓글에 대한 수정/삭제 권한이 없습니다.");
        }
    }
}