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
        //  데이터 조회 쿼리
        List<ContentResponseDto> contentList = queryFactory
                .select(new QContentResponseDto(
                        contents.id,
                        contents.title,
                        contents.createdBy,
                        contents.viewCount,
                        contents.createdDate
                ))
                .from(contents)
                .where(
                        titleContains(condition.getTitle()),
                        usernameContains(condition.getUsername()),
                        dateBetween(condition.getStartDate(), condition.getEndDate())
                )
                .offset(pageable.getOffset())   // 시작 지점
                .limit(pageable.getPageSize())  // 페이지 사이즈
                .orderBy(contents.id.desc())    // 최신순 정렬
                .fetch();

        // Count 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(contents.count())
                .from(contents)
                .where(
                        titleContains(condition.getTitle()),
                        usernameContains(condition.getUsername()),
                        dateBetween(condition.getStartDate(), condition.getEndDate())
                );

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
