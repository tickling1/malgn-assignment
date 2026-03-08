package com.malgn.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Contents extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 50)
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member author;

    @LastModifiedBy
    @Column(length = 50)
    private String lastModifiedBy;

    @OneToMany(mappedBy = "parentContent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

    @Builder
    public Contents(String title, String description, Member author) {
        this.title = title;
        this.description = description;
        this.viewCount = 0L;
        this.author = author;
    }

    public void incrementViewCount() { this.viewCount++; }
    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
