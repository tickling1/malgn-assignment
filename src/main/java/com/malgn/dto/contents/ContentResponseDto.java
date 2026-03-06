package com.malgn.dto.contents;


import com.malgn.domain.Contents;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ContentResponseDto {
    private Long id;
    private String title;
    private String createdBy; // 작성자 이름(username)
    private Long viewCount;
    private LocalDateTime createdDate;

    public static ContentResponseDto from(Contents entity) {
        ContentResponseDto dto = new ContentResponseDto();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.createdBy = entity.getCreatedBy();
        dto.viewCount = entity.getViewCount();
        dto.createdDate = entity.getCreatedDate();
        return dto;
    }
}
