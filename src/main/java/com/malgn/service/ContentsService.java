package com.malgn.service;

import com.malgn.configure.security.CustomUserDetails;
import com.malgn.domain.Contents;
import com.malgn.domain.Member;
import com.malgn.dto.contents.ContentDetailResponseDto;
import com.malgn.dto.contents.ContentRequestDto;
import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.dto.contents.ContentSearchCondition;
import com.malgn.exception.BusinessException; // 공통 예외 도입
import com.malgn.exception.ErrorCode;         // 에러 코드 도입
import com.malgn.repository.ContentsRepository;
import com.malgn.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;
    private final MemberRepository memberRepository;

    /**
     * 1. 콘텐츠 추가
     * getReferenceById를 사용하여 불필요한 Member Select 쿼리를 방지합니다.
     */
    @Transactional
    public Long createContent(ContentRequestDto dto, CustomUserDetails user) {
        // 인증 객체에서 ID를 추출하여 프록시 객체 생성 (Insert 성능 최적화)
        Member author = memberRepository.getReferenceById(user.getId());

        Contents content = Contents.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .author(author)
                .build();

        return contentsRepository.save(content).getId();
    }

    /**
     * 2. 콘텐츠 목록 전체 조회 - 페이징
     */
    public Page<ContentResponseDto> getContentsList(Pageable pageable) {
        return contentsRepository.findAll(pageable)
                .map(ContentResponseDto::from);
    }

    /**
     * 3. 동적 검색 (QueryDSL)
     */
    public Page<ContentResponseDto> getContentsListWithCond(ContentSearchCondition condition, Pageable pageable) {
        return contentsRepository.searchContents(condition, pageable);
    }

    /**
     * 4. 콘텐츠 상세 조회 (조회수 증가 포함)
     */
    @Transactional
    public ContentDetailResponseDto getContentDetail(Long id) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        content.incrementViewCount(); // 변경 감지(Dirty Check) 작동
        return ContentDetailResponseDto.from(content);
    }

    /**
     * 5. 콘텐츠 수정
     */
    @Transactional
    public void updateContent(Long id, ContentRequestDto dto, CustomUserDetails user) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(content.getAuthor().getId(), user);

        content.update(dto.getTitle(), dto.getDescription());
    }

    /**
     * 6. 콘텐츠 삭제
     */
    @Transactional
    public void deleteContent(Long id, CustomUserDetails user) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(content.getAuthor().getId(), user);

        contentsRepository.delete(content);
    }

    /**
     * 권한 검증 공통 메서드 (CustomUserDetails 통합)
     */
    private void validateAuthorOrAdmin(Long authorId, CustomUserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        if (!isAdmin && !Objects.equals(authorId, user.getId())) {
            throw new BusinessException(ErrorCode.NOT_AUTHOR);
        }
    }
}