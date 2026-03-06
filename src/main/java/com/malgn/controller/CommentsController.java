package com.malgn.controller;

import com.malgn.dto.comments.CommentRequestDto;
import com.malgn.service.CommentsService;
import com.malgn.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsService commentsService;

    /**
     * 1. 댓글 및 대댓글 등록
     * POST /api/comments
     */
    @PostMapping("/contents/{contentId}/comments")
    public ResponseEntity<Long> create(
            @PathVariable Long contentId,
            @RequestBody CommentRequestDto dto) {

        // DTO에 contentId를 수동으로 세팅해주거나,
        // 서비스 파라미터에 contentId를 따로 넘기도록 수정
        dto.setContentId(contentId);
        Long commentId = commentsService.createComment(dto);

        return ResponseEntity.ok(commentId);
    }

    /**
     * 2. 댓글 수정
     * PUT /api/comments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody CommentRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        commentsService.updateComment(id, dto.getContent(), userDetails);
        return ResponseEntity.ok().build();
    }

    /**
     * 3. 댓글 삭제
     * DELETE /api/comments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        commentsService.deleteComment(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
