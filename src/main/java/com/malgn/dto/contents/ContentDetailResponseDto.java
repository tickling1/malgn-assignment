package com.malgn.dto.contents;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import com.malgn.dto.comments.CommentResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
// JSON 출력 순서를 논리적으로 조정 (수정자 정보를 작성자 정보 뒤에 배치)
@JsonPropertyOrder({
        "id", "title", "description", "viewCount",
        "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate",
        "comments"
})
@Schema(description = "콘텐츠 상세 및 계층형 댓글 응답 정보")
public class ContentDetailResponseDto {

    @Schema(description = "콘텐츠 고유 번호", example = "10")
    private Long id;

    @Schema(description = "콘텐츠 제목", example = "맑은기술 과제 진행 현황")
    private String title;

    @Schema(description = "콘텐츠 상세 본문", example = "현재까지 Swagger 설정 및 QueryDSL 검색 구현을 완료")
    private String description;

    @Schema(description = "최초 작성자 아이디", example = "developer_01")
    private String createdBy;

    @Schema(description = "최종 수정자 아이디", example = "admin_master")
    private String lastModifiedBy; // 추가

    @Schema(description = "조회수", example = "256")
    private Long viewCount;

    @Schema(description = "최초 작성 일시", example = "2026-03-07T23:50:00")
    private LocalDateTime createdDate;

    @Schema(description = "최종 수정 일시", example = "2026-03-08T10:30:00")
    private LocalDateTime lastModifiedDate; // 추가

    @Schema(description = "최상위 댓글 리스트 (각 댓글 내부에 대댓글 포함)")
    private List<CommentResponseDto> comments;

    public static ContentDetailResponseDto from(Contents contents) {
        ContentDetailResponseDto dto = new ContentDetailResponseDto();
        dto.id = contents.getId();
        dto.title = contents.getTitle();
        dto.description = contents.getDescription();
        dto.viewCount = contents.getViewCount();

        // Auditing 정보 매핑
        dto.createdBy = contents.getCreatedBy();
        dto.createdDate = contents.getCreatedDate();
        dto.lastModifiedBy = contents.getLastModifiedBy(); // 추가
        dto.lastModifiedDate = contents.getLastModifiedDate(); // 추가

        // 계층형 댓글 처리 로직 (기존 유지)
        dto.comments = contents.getComments().stream()
                .filter(comment -> comment.getParentComment() == null)
                .sorted(Comparator.comparing(Comments::getCreatedDate))
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        return dto;
    }
}
