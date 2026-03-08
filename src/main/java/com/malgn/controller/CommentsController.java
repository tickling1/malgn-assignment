package com.malgn.controller;

import com.malgn.dto.comments.CommentRequestDto;
import com.malgn.exception.ErrorResponse;
import com.malgn.service.CommentsService;
import com.malgn.configure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comments", description = "계층형 댓글(대댓글) 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentsController {

    private final CommentsService commentsService;

    @Operation(summary = "댓글 작성", description = "댓글 및 대댓글을 통합 등록합니다. 대댓글 작성 시 상위 댓글의 ID(parentId)를 json에 포함하세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공 (생성된 댓글 ID 반환)",
                    content = @Content(schema = @Schema(implementation = Long.class), examples = @ExampleObject(value = "5"))),
            @ApiResponse(responseCode = "400", description = "입력값 오류 (INVALID_INPUT_VALUE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "C001 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:55:00",
                                      "status": 400,
                                      "code": "C001",
                                      "message": "올바르지 않은 입력값입니다.",
                                      "path": "/api/contents/10/comments"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "게시글 또는 부모 댓글 없음 (RESOURCE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "C002 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:55:00",
                                      "status": 404,
                                      "code": "C002",
                                      "message": "해당 리소스를 찾을 수 없습니다.",
                                      "path": "/api/contents/10/comments"
                                    }
                                    """)))
    })
    @PostMapping("/contents/{contentId}/comments")
    public ResponseEntity<Long> create(
            @Parameter(description = "대상 게시글 ID", example = "10") @PathVariable Long contentId,
            @RequestBody CommentRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        dto.setContentId(contentId);
        Long commentId = commentsService.createComment(dto, userDetails);
        return ResponseEntity.ok(commentId);
    }



    @Operation(summary = "댓글 수정", description = "댓글 본문을 수정합니다. 작성자 본인만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (NOT_AUTHOR)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "B001 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:56:00",
                                      "status": 403,
                                      "code": "B001",
                                      "message": "작성자만 수정/삭제할 수 있습니다.",
                                      "path": "/api/comments/5"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "댓글 없음 (RESOURCE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "댓글 미존재 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:56:00",
                                      "status": 404,
                                      "code": "C002",
                                      "message": "해당 리소스를 찾을 수 없습니다.",
                                      "path": "/api/comments/5"
                                    }
                                    """)))
    })
    @PutMapping("/comments/{id}")
    public ResponseEntity<Void> update(
            @Parameter(description = "수정할 댓글 ID", example = "5") @PathVariable Long id,
            @RequestBody CommentRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentsService.updateComment(id, dto.getContent(), userDetails);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 작성자 본인 또는 관리자만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (NOT_AUTHOR)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "B001 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:57:00",
                                      "status": 403,
                                      "code": "B001",
                                      "message": "작성자만 수정/삭제할 수 있습니다.",
                                      "path": "/api/comments/5"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "삭제 대상 없음 (RESOURCE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "삭제 리소스 미존재 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:57:00",
                                      "status": 404,
                                      "code": "C002",
                                      "message": "해당 리소스를 찾을 수 없습니다.",
                                      "path": "/api/comments/5"
                                    }
                                    """)))
    })
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 댓글 ID", example = "5") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentsService.deleteComment(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}