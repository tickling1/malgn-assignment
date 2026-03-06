package com.malgn.repository;

import com.malgn.domain.Comments;
import com.malgn.domain.Contents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    // 1. 특정 게시글의 모든 댓글 조회 (작성순 정렬)
    // 사실 Contents 엔티티에서 @OneToMany로 가져오지만,
    // 댓글만 따로 관리하거나 페이징할 때 유용합니다.
    List<Comments> findByParentContentOrderByCreatedDateAsc(Contents content);

    // 2. 특정 사용자가 쓴 댓글 목록 조회 (마이페이지용)
    List<Comments> findByCreatedBy(String loginId);

    // 3. (심화) 특정 부모 댓글에 달린 대댓글들만 조회
    // List<Comments> findByParentCommentId(Long parentId);
}
