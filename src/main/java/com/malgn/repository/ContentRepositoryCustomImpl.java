package com.malgn.repository;

import com.malgn.dto.contents.ContentResponseDto;
import com.malgn.dto.contents.ContentSearchCondition;
import com.malgn.dto.contents.QContentResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.malgn.domain.QContents.contents;

@RequiredArgsConstructor
public class ContentRepositoryCustomImpl implements ContentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ContentResponseDto> searchContents(ContentSearchCondition condition, Pageable pageable) {
        // 1. 데이터 조회 쿼리 (DTO 생성자 파라미터 순서와 100% 일치해야 함)
        List<ContentResponseDto> contentList = queryFactory
                .select(new QContentResponseDto(
                        contents.id,
                        contents.title,
                        contents.createdBy,
                        contents.lastModifiedBy,
                        contents.viewCount,
                        contents.createdDate,
                        contents.lastModifiedDate
                ))
                .from(contents)
                .where(
                        titleContains(condition.getTitle()),
                        usernameContains(condition.getUsername()),
                        dateBetween(condition.getStartDate(), condition.getEndDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(contents.createdDate.desc()) // 최신 등록순 정렬
                .fetch();

        // 2. Count 쿼리 (기존과 동일하지만 가독성을 위해 최적화)
        JPAQuery<Long> countQuery = queryFactory
                .select(contents.count())
                .from(contents)
                .where(
                        titleContains(condition.getTitle()),
                        usernameContains(condition.getUsername()),
                        dateBetween(condition.getStartDate(), condition.getEndDate())
                );

        // PageableExecutionUtils는 페이지 수가 모자라거나 마지막 페이지일 때 count 쿼리를 생략해주는 최적화를 지원합니다.
        return PageableExecutionUtils.getPage(contentList, pageable, countQuery::fetchOne);
    }

    // == 조건 조립 메서드 ==
    // 메서드 분리
    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? contents.title.contains(title) : null;
    }

    private BooleanExpression usernameContains(String username) {
        return StringUtils.hasText(username) ? contents.createdBy.contains(username) : null;
    }

    private BooleanExpression dateBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return null;

        // dto에서 받은 LocalDate를 LocalDateTime으로 변환 (00:00:00 ~ 23:59:59.999)
        return contents.createdDate.between(
                start.atStartOfDay(),
                end.atTime(LocalTime.MAX)
        );
    }
}
