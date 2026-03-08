package com.malgn.dto.contents;


import com.malgn.domain.Contents;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "콘텐츠 목록 조회 응답 정보")
public class ContentResponseDto {

    @Schema(description = "콘텐츠 고유 번호 (ID)", example = "1")
    private Long id;

    @Schema(description = "콘텐츠 제목", example = "맑은기술 백엔드 과제 구현 사항")
    private String title;

    @Schema(description = "작성자", example = "홍길동")
    private String createdBy; // 작성자 이름(username)

    @Schema(description = "최종 수정자 아이디", example = "admin_master")
    private String lastModifiedBy;

    @Schema(description = "조회수", example = "42")
    private Long viewCount;

    @Schema(description = "최초 작성 일시", example = "2026-03-07T23:45:00")
    private LocalDateTime createdDate;

    @Schema(description = "최종 수정 일시", example = "2026-03-08T10:30:00")
    private LocalDateTime lastModifiedDate;

    // 기존 상세 조회용 메서드 (Entity to DTO)
    public static ContentResponseDto from(Contents entity) {
        ContentResponseDto dto = new ContentResponseDto();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.createdBy = entity.getCreatedBy();
        dto.lastModifiedBy = entity.getLastModifiedBy();
        dto.viewCount = entity.getViewCount();
        dto.createdDate = entity.getCreatedDate();
        dto.lastModifiedDate = entity.getLastModifiedDate();
        return dto;
    }

    // QueryDSL 전용 생성자 (Direct Projection용)
    @QueryProjection
    public ContentResponseDto(Long id, String title, String createdBy, String lastModifiedBy,
                              Long viewCount, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.title = title;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.viewCount = viewCount;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }
}
