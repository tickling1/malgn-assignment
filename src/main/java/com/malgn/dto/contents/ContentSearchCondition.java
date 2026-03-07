package com.malgn.dto.contents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "콘텐츠 동적 검색 조건")
public class ContentSearchCondition {

    @Schema(description = "검색할 제목 키워드 (포함 검색)", example = "공지")
    private String title;      // 제목

    @Schema(description = "작성자 닉네임(username)", example = "홍길동")
    private String username;   // 작성자

    @Schema(description = "조회 시작일", example = "2026-03-05")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "조회 종료일", example = "2026-03-09")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
