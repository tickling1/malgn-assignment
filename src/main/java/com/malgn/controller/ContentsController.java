package com.malgn.controller;

import com.malgn.dto.contents.ContentDetailResponseDto;
import com.malgn.dto.contents.ContentRequestDto;
import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.dto.contents.ContentSearchCondition;
import com.malgn.exception.ErrorResponse;
import com.malgn.service.ContentsService;
import com.malgn.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Contents", description = "콘텐츠(게시글) 관리 및 QueryDSL 동적 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentsController {

    private final ContentsService contentsService;

    @Operation(summary = "콘텐츠 전체 목록 조회", description = "단순 페이징 처리가 된 콘텐츠 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<Page<ContentResponseDto>> getList(
            @Parameter(description = "페이징 및 정렬 정보 (예: page=0&size=10&sort=createdDate,desc)")
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(contentsService.getContentsList(pageable));
    }

    @Operation(summary = "콘텐츠 동적 검색", description = "QueryDSL을 사용하여 제목, 작성자, 기간별로 콘텐츠를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<Page<ContentResponseDto>> getSearchList(
            @ModelAttribute ContentSearchCondition condition,
            @Parameter(description = "페이징 정보") @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(contentsService.getContentsListWithCond(condition, pageable));
    }



    @Operation(summary = "콘텐츠 상세 조회", description = "ID로 콘텐츠를 상세 조회하며 조회 시 조회수가 1 증가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "콘텐츠 없음 (RESOURCE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "리소스 미존재 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:30:00",
                                      "status": 404,
                                      "code": "C002",
                                      "message": "해당 리소스를 찾을 수 없습니다.",
                                      "path": "/api/contents/999"
                                    }
                                    """)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContentDetailResponseDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(contentsService.getContentDetail(id));
    }

    @Operation(summary = "콘텐츠 등록", description = "새로운 콘텐츠를 등록합니다. 인증된 사용자 세션 정보가 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공 (생성된 ID 반환)",
                    content = @Content(schema = @Schema(implementation = Long.class), examples = @ExampleObject(value = "1"))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 (INVALID_INPUT_VALUE)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "입력값 오류 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:30:00",
                                      "status": 400,
                                      "code": "C001",
                                      "message": "올바르지 않은 입력값입니다.",
                                      "path": "/api/contents"
                                    }
                                    """)))
    })
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody ContentRequestDto dto) {
        return ResponseEntity.ok(contentsService.createContent(dto));
    }

    @Operation(summary = "콘텐츠 수정", description = "작성자 본인 또는 관리자만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 부족 (NOT_AUTHOR)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "작성자 불일치 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:35:00",
                                      "status": 403,
                                      "code": "B001",
                                      "message": "작성자만 수정/삭제할 수 있습니다.",
                                      "path": "/api/contents/1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "수정 대상 없음 (RESOURCE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "수정 대상 미존재 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:35:00",
                                      "status": 404,
                                      "code": "C002",
                                      "message": "해당 리소스를 찾을 수 없습니다.",
                                      "path": "/api/contents/1"
                                    }
                                    """)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody ContentRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        contentsService.updateContent(id, dto, userDetails.getId(), userDetails.getAuthorities());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "콘텐츠 삭제", description = "작성자 본인 또는 관리자만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (NOT_AUTHOR)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "삭제 권한 없음 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:40:00",
                                      "status": 403,
                                      "code": "B001",
                                      "message": "작성자만 수정/삭제할 수 있습니다.",
                                      "path": "/api/contents/1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "삭제 대상 없음 (RESOURCE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "삭제 대상 미존재 예시", value = """
                                    {
                                      "timestamp": "2026-03-07T23:40:00",
                                      "status": 404,
                                      "code": "C002",
                                      "message": "해당 리소스를 찾을 수 없습니다.",
                                      "path": "/api/contents/1"
                                    }
                                    """)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        contentsService.deleteContent(id, userDetails.getId(), userDetails.getAuthorities());
        return ResponseEntity.noContent().build();
    }
}