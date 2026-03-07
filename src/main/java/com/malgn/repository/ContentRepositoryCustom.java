package com.malgn.repository;

import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.dto.contents.ContentSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentRepositoryCustom {
    Page<ContentResponseDto> searchContents(ContentSearchCondition condition, Pageable pageable);
}
