package com.malgn.dto.contents;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import com.malgn.dto.comments.CommentResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@JsonPropertyOrder({ "id", "title", "description", "createdBy", "createdDate", "viewCount", "comments" })
public class ContentDetailResponseDto {
    private Long id;
    private String title;
    private String description;
    private String createdBy;
    private Long viewCount;
    private LocalDateTime createdDate;
    private List<CommentResponseDto> comments; // 계층형 댓글

    public static ContentDetailResponseDto from(Contents contents) {
        ContentDetailResponseDto dto = new ContentDetailResponseDto();
        dto.id = contents.getId();
        dto.title = contents.getTitle();
        dto.description = contents.getDescription();
        dto.createdBy = contents.getCreatedBy();
        dto.createdDate = contents.getCreatedDate();
        dto.viewCount = contents.getViewCount();


        dto.comments = contents.getComments().stream()
                .filter(comment -> comment.getParentComment() == null) // 필드명 반영
                .sorted(Comparator.comparing(Comments::getCreatedDate))
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        return dto;
    }
}
