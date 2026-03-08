package com.malgn.dto.comments;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 및 대댓글 등록 요청 DTO")
public class CommentRequestDto {

    @Schema(description = "댓글이 달릴 게시글(콘텐츠) ID", example = "10")
    private Long contentId;    // 댓글이 달릴 게시글 ID

    @Schema(description = "부모 댓글 ID (일반 댓글은 null, 대댓글 작성 시에만 부모 ID 전달)", example = "1", nullable = true)
    private Long parentId;     // 부모 댓글 ID (대댓글일 경우에만 전달, 일반 댓글이면 null)

    @Schema(description = "댓글 본문 내용", example = "정말 유익한 정보입니다")
    private String content;    // 댓글 내용
}
