package com.malgn.dto.contents;

import com.malgn.domain.Contents;
import com.malgn.dto.comments.CommentResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ContentDetailResponseDto {
    private Long id;
    private String title;
    private String description;
    private String createdBy;
    private Long viewCount;
    private LocalDateTime createdDate;
    private List<CommentResponseDto> comments; // 계층형 댓글 구조

    public static ContentDetailResponseDto from(Contents entity) {
        ContentDetailResponseDto dto = new ContentDetailResponseDto();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.description = entity.getDescription();
        dto.createdBy = entity.getCreatedBy();
        dto.viewCount = entity.getViewCount();
        dto.createdDate = entity.getCreatedDate();

        // [핵심] 부모 댓글이 없는(null) 최상위 댓글들만 필터링해서 변환
        dto.comments = entity.getComments().stream()
                .filter(comment -> comment.getParentComment() == null) // 부모 없는 놈이 대장
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        return dto;
    }
}
