package com.malgn.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Comments extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 50)
    private String createdBy; // 생성자 (로그인 ID 자동 저장)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 작성자 객체 직접 참조

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Contents parentContent; // 어느 게시글의 댓글인가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comments parentComment; // 부모 댓글 (대댓글일 경우)

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comments> children = new ArrayList<>(); // 내 밑에 달린 대댓글들

    @Builder
    public Comments(String content, Contents parentContent, Comments parentComment, Member member) {
        this.content = content;
        this.parentContent = parentContent;
        this.parentComment = parentComment;
        this.member = member;
    }

    public void update(String content) {
        this.content = content;
    }
}
