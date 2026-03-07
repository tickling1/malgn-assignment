package com.malgn.dto.comments;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentRequestDto {

    private Long contentId;    // 댓글이 달릴 게시글 ID
    private Long parentId;     // 부모 댓글 ID (대댓글일 경우에만 전달, 일반 댓글이면 null)
    private String content;    // 댓글 내용
}
