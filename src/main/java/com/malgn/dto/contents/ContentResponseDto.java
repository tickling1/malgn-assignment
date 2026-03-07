package com.malgn.dto.contents;


import com.malgn.domain.Contents;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ContentResponseDto {
    private Long id;
    private String title;
    private String createdBy; // 작성자 이름(username)
    private Long viewCount;
    private LocalDateTime createdDate;

    // 기존 상세 조회용 메서드 (Entity to DTO)
    public static ContentResponseDto from(Contents entity) {
        ContentResponseDto dto = new ContentResponseDto();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.createdBy = entity.getCreatedBy();
        dto.viewCount = entity.getViewCount();
        dto.createdDate = entity.getCreatedDate();
        return dto;
    }

    // QueryDSL 전용 생성자 (Direct Projection용)
    @QueryProjection
    public ContentResponseDto(Long id, String title, String createdBy, Long viewCount, LocalDateTime createdDate) {
        this.id = id;
        this.title = title;
        this.createdBy = createdBy;
        this.viewCount = viewCount;
        this.createdDate = createdDate;
    }
}
