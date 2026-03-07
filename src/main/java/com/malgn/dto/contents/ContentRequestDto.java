package com.malgn.dto.contents;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "콘텐츠 등록 및 수정 요청 DTO")
public class ContentRequestDto {

    @Schema(description = "콘텐츠 제목 (최대 100자)", example = "맑은기술 신규 프로젝트 공지")
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Schema(description = "콘텐츠 본문 내용", example = "안녕하세요. 반갑습니다")
    @NotBlank(message = "내용은 필수입니다.")
    private String description;

    @Builder
    public ContentRequestDto(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
