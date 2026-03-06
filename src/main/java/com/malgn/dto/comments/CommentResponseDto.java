package com.malgn.dto.comments;

import com.malgn.domain.Comments;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentResponseDto {
    private Long id;
    private String content;
    private String createdBy; // @CreatedBy로 저장된 작성자 ID
    private LocalDateTime createdDate;
    private List<CommentResponseDto> children = new ArrayList<>(); // 대댓글 리스트

    public static CommentResponseDto from(Comments entity) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.id = entity.getId();
        dto.content = entity.getContent();
        dto.createdBy = entity.getCreatedBy();
        dto.createdDate = entity.getCreatedDate();

        // 자식 엔티티들을 다시 DTO로 변환 (재귀 호출)
        if (entity.getChildren() != null) {
            dto.children = entity.getChildren().stream()
                    .map(CommentResponseDto::from)
                    .collect(Collectors.toList());
        }
        return dto;
    }
}
