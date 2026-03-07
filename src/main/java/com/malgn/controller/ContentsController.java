package com.malgn.controller;

import com.malgn.dto.contents.ContentDetailResponseDto;
import com.malgn.dto.contents.ContentRequestDto;
import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.dto.contents.ContentSearchCondition;
import com.malgn.service.ContentsService;
import com.malgn.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentsController {

    private final ContentsService contentsService;

    /**
     * 1. 콘텐츠 전체 목록 조회 (단순 페이징)
     * GET /api/contents?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<ContentResponseDto>> getList(
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        // 검색 조건 없이 페이징만 수행하는 서비스 호출
        Page<ContentResponseDto> responsePage = contentsService.getContentsList(pageable);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * 2. 콘텐츠 검색 목록 조회 (QueryDSL 동적 검색)
     * GET /api/contents/search?title=공지&username=admin&startDate=2026-02-07T00:00:00&endDate=2026-03-07T23:59:59&page=0&size=10
     */
    @GetMapping("/search") // 👈 경로를 분리하여 중복 매핑 방지
    public ResponseEntity<Page<ContentResponseDto>> getSearchList(
            @ModelAttribute ContentSearchCondition condition,
            @PageableDefault(size = 10) Pageable pageable) {

        // QueryDSL 기반의 동적 검색 서비스 호출
        Page<ContentResponseDto> result = contentsService.getContentsListWithCond(condition, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 3. 콘텐츠 상세 조회 (조회수 증가 포함)
     * GET /api/contents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContentDetailResponseDto> getDetail(@PathVariable Long id) {
        ContentDetailResponseDto detail = contentsService.getContentDetail(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * 4. 콘텐츠 등록
     * POST /api/contents
     */
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody ContentRequestDto dto) {
        Long contentId = contentsService.createContent(dto);
        return ResponseEntity.ok(contentId);
    }


    /**
     * 5. 콘텐츠 수정
     * PUT /api/contents/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody ContentRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 로그인 유저의 name과 권한 정보를 서비스로 넘깁니다.
        contentsService.updateContent(id, dto, userDetails.getName(), userDetails.getAuthorities());
        return ResponseEntity.ok().build();
    }

    /**
     * 6. 콘텐츠 삭제
     * DELETE /api/contents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        contentsService.deleteContent(id, userDetails.getName(), userDetails.getAuthorities());
        return ResponseEntity.noContent().build();
    }
}

