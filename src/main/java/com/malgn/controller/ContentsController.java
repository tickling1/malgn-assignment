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
     * 1. 콘텐츠 목록 조회 (페이징 & 검색)
     */
    @GetMapping
    public ResponseEntity<Page<ContentResponseDto>> getList(
            @ModelAttribute ContentSearchCondition condition,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        // 서비스가 이미 Page<ContentResponseDto>를 반환하므로 바로 받습니다.
        Page<ContentResponseDto> responsePage = contentsService.getContentsList(condition, pageable);

        return ResponseEntity.ok(responsePage);
    }
    /**
     * 2. 콘텐츠 상세 조회 (조회수 증가 포함)
     * GET /api/contents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContentDetailResponseDto> getDetail(@PathVariable Long id) {
        ContentDetailResponseDto detail = contentsService.getContentDetail(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * 3. 콘텐츠 등록
     * POST /api/contents
     */
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody ContentRequestDto dto) {
        Long contentId = contentsService.createContent(dto);
        return ResponseEntity.ok(contentId);
    }


    /**
     * 4. 콘텐츠 수정
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
     * 5. 콘텐츠 삭제
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

