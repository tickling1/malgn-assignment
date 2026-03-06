package com.malgn.service;

import com.malgn.domain.Contents;
import com.malgn.dto.contents.ContentDetailResponseDto;
import com.malgn.dto.contents.ContentRequestDto;
import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.dto.contents.ContentSearchCondition;
import com.malgn.repository.ContentsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;

    /**
     * 1. 콘텐츠 추가
     */
    @Transactional
    public Long createContent(ContentRequestDto dto) {

        Contents content = Contents.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .build();
        return contentsRepository.save(content).getId();
    }

    /**
     * 2. 콘텐츠 목록 조회 - 페이징 & 검색 포함
     */
    @Transactional(readOnly = true)
    public Page<ContentResponseDto> getContentsList(ContentSearchCondition condition, Pageable pageable) {

        // A. 날짜 범위 검색이 최우선
        if (condition.getStartDate() != null && condition.getEndDate() != null) {
            return contentsRepository.findByCreatedDateBetween(
                            condition.getStartDate(), condition.getEndDate(), pageable)
                    .map(ContentResponseDto::from);
        }

        // B. 키워드 검색 (제목 또는 작성자 이름)
        if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
            if ("username".equals(condition.getType())) {
                return contentsRepository.findByCreatedByContaining(condition.getKeyword(), pageable)
                        .map(ContentResponseDto::from);
            }
            // 기본값은 제목 검색
            return contentsRepository.findByTitleContaining(condition.getKeyword(), pageable)
                    .map(ContentResponseDto::from);
        }

        // C. 조건 없을 시 전체 목록 페이징 조회
        return contentsRepository.findAll(pageable)
                .map(ContentResponseDto::from);
    }

    /**
     * 3. 콘텐츠 상세 조회 - 조회수 증가 및 댓글 포함
     */
    @Transactional
    public ContentDetailResponseDto getContentDetail(Long id) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("콘텐츠를 찾을 수 없습니다. ID: " + id));

        // 상세 조회 시 조회수 1 증가
        content.incrementViewCount();

        // DTO로 변환 (이때 DTO 내부에서 댓글 계층 구조화가 일어납니다)
        return ContentDetailResponseDto.from(content);
    }

    /**
     * 4. 콘텐츠 수정
     */
    @Transactional
    public void updateContent(Long id, ContentRequestDto dto, String currentName, Collection<? extends GrantedAuthority> authorities) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));

        // 권한 체크 로직 호출
        validateAuthorOrAdmin(content.getCreatedBy(), currentName, authorities);

        content.update(dto.getTitle(), dto.getDescription()); // 엔티티에 update 메서드 필요
    }

    /**
     * 5. 콘텐츠 삭제
     */
    @Transactional
    public void deleteContent(Long id, String currentName, Collection<? extends GrantedAuthority> authorities) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다."));

        // 권한 체크 로직 호출
        validateAuthorOrAdmin(content.getCreatedBy(), currentName, authorities);

        contentsRepository.delete(content);
    }

    /**
     * 작성자 본인 혹은 관리자인지 검증하는 공통 메서드
     */
    private void validateAuthorOrAdmin(String authorName, String currentName, Collection<? extends GrantedAuthority> authorities) {
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !authorName.equals(currentName)) {
            throw new IllegalStateException("수정/삭제 권한이 없습니다.");
        }
    }
}
