package com.malgn.service;

import com.malgn.domain.Contents;
import com.malgn.dto.contents.ContentDetailResponseDto;
import com.malgn.dto.contents.ContentRequestDto;
import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.dto.contents.ContentSearchCondition;
import com.malgn.exception.BusinessException; // 공통 예외 도입
import com.malgn.exception.ErrorCode;         // 에러 코드 도입
import com.malgn.repository.ContentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;

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
        if (condition.getStartDate() != null && condition.getEndDate() != null) {
            return contentsRepository.findByCreatedDateBetween(
                            condition.getStartDate(), condition.getEndDate(), pageable)
                    .map(ContentResponseDto::from);
        }

        if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
            if ("username".equals(condition.getType())) {
                return contentsRepository.findByCreatedByContaining(condition.getKeyword(), pageable)
                        .map(ContentResponseDto::from);
            }
            return contentsRepository.findByTitleContaining(condition.getKeyword(), pageable)
                    .map(ContentResponseDto::from);
        }

        return contentsRepository.findAll(pageable)
                .map(ContentResponseDto::from);
    }

    /**
     * 3. 콘텐츠 상세 조회
     */
    @Transactional
    public ContentDetailResponseDto getContentDetail(Long id) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        content.incrementViewCount();
        return ContentDetailResponseDto.from(content);
    }

    /**
     * 4. 콘텐츠 수정
     */
    @Transactional
    public void updateContent(Long id, ContentRequestDto dto, String currentName, Collection<? extends GrantedAuthority> authorities) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(content.getCreatedBy(), currentName, authorities);

        content.update(dto.getTitle(), dto.getDescription());
    }

    /**
     * 5. 콘텐츠 삭제
     */
    @Transactional
    public void deleteContent(Long id, String currentName, Collection<? extends GrantedAuthority> authorities) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(content.getCreatedBy(), currentName, authorities);

        contentsRepository.delete(content);
    }

    /**
     * 권한 검증 공통 메서드
     */
    private void validateAuthorOrAdmin(String authorName, String currentName, Collection<? extends GrantedAuthority> authorities) {
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        if (!isAdmin && !authorName.equals(currentName)) {
            throw new BusinessException(ErrorCode.NOT_AUTHOR);
        }
    }
}