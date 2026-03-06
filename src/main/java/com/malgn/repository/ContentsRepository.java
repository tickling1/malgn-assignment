package com.malgn.repository;

import com.malgn.domain.Contents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ContentsRepository extends JpaRepository<Contents, Long> {
    // 1. 기본 목록 조회용 (검색 조건 없을 때) -> findAll(Pageable) 사용

    // 2. 제목 검색 + 페이징
    Page<Contents> findByTitleContaining(String title, Pageable pageable);

    // 3. 작성자(username) 검색 + 페이징
    Page<Contents> findByCreatedByContaining(String username, Pageable pageable);

    // 4. 날짜 범위 검색 + 페이징
    Page<Contents> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
