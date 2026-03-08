package com.malgn.service;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;
    private final MemberRepository memberRepository;

    /**
     * 1. 콘텐츠 추가
     */
    @Transactional
    public Long createContent(ContentRequestDto dto) {
        // 1. 현재 Spring Security Context에서 로그인한 유저의 ID(loginId) 추출
        String currentLoginId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        // 2. DB에서 해당 멤버 엔티티 조회
        Member author = memberRepository.findByLoginId(currentLoginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. 작성자 정보를 포함하여 콘텐츠 빌드
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
    @Transactional(readOnly = true)
    public Page<ContentResponseDto> getContentsList(Pageable pageable) {
        // 검색 조건 없이 단순히 전체 목록을 페이징하여 반환
        return contentsRepository.findAll(pageable)
                .map(ContentResponseDto::from);
    }

    /**
     * 3. 콘텐츠 목록 조회 및 동적 검색 (QueryDSL 적용)
     */
    @Transactional(readOnly = true)
    public Page<ContentResponseDto> getContentsListWithCond(ContentSearchCondition condition, Pageable pageable) {
        return contentsRepository.searchContents(condition, pageable);
    }

    /**
     * 4. 콘텐츠 상세 조회
     * 증가 값이 있으므로 readOnly 건들지 말 것
     */
    @Transactional
    public ContentDetailResponseDto getContentDetail(Long id) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        content.incrementViewCount();
        return ContentDetailResponseDto.from(content);
    }

    /**
     * 5. 콘텐츠 수정
     */
    @Transactional
    public void updateContent(Long id, ContentRequestDto dto, Long currentMemberId, Collection<? extends GrantedAuthority> authorities) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(content.getAuthor().getId(), currentMemberId, authorities);

        content.update(dto.getTitle(), dto.getDescription());
    }

    /**
     * 6. 콘텐츠 삭제
     */
    @Transactional
    public void deleteContent(Long id, Long currentMemberId, Collection<? extends GrantedAuthority> authorities) {
        Contents content = contentsRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        validateAuthorOrAdmin(content.getAuthor().getId(), currentMemberId, authorities);

        contentsRepository.delete(content);
    }


    /**
     * 권한 검증 공통 메서드
     * @param authorId 콘텐츠 작성자의 PK (Long)
     * @param currentMemberId 현재 로그인한 사용자의 PK (Long)
     * @param authorities 현재 로그인한 사용자의 권한 목록
     */
    @Transactional(readOnly = true)
    public void validateAuthorOrAdmin(Long authorId, Long currentMemberId, Collection<? extends GrantedAuthority> authorities) {

        // 1. 관리자 여부 확인 (ROLE_ prefix 주의)
        boolean isAdmin = authorities.stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        // 2. 관리자가 아니면서, 작성자 PK와 로그인 유저 PK가 다르면 예외 발생
        if (!isAdmin && !Objects.equals(authorId, currentMemberId)) {
            throw new BusinessException(ErrorCode.NOT_AUTHOR);
        }
    }
}