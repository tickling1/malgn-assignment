package com.malgn.dto.comments;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.malgn.domain.Comments;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@JsonPropertyOrder({ "id", "content", "createdBy", "createdDate", "children" })
@Schema(description = "계층형 댓글 응답 정보")
public class CommentResponseDto {

    @Schema(description = "댓글 고유 ID", example = "1")
    private Long id;

    @Schema(description = "댓글 내용", example = "작성하신 내용 중 QueryDSL 설정 부분이 궁금합니다.")
    private String content;

    @Schema(description = "작성자 아이디", example = "tester_user")
    private String createdBy; // @CreatedBy로 저장된 작성자 ID

    @Schema(description = "작성 일시", example = "2026-03-07T23:55:00")
    private LocalDateTime createdDate;

    // 비어있을 경우(대댓글이 없을 경우) JSON 응답에서 필드를 아예 숨김
    @Schema(description = "대댓글(자식) 리스트 (자식이 없을 경우 응답에서 제외됨)")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
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
