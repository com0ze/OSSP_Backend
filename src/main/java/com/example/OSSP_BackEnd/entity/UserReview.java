package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_review")
@EntityListeners(AuditingEntityListener.class)
public class UserReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    @Column(name = "score", precision = 2, scale = 1)
    private BigDecimal score;

    @Column(name = "comments", length = 255)
    private String comments;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private MatchHistory matchHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    private UserReview(
            User reviewer,
            User reviewee,
            BigDecimal score,
            String comments,
            MatchHistory matchHistory
    ) {
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.score = score;
        this.comments = comments;
        this.matchHistory = matchHistory;
    }

    public static UserReview create(
            User reviewer,
            User reviewee,
            BigDecimal score,
            String comments,
            MatchHistory matchHistory
    ) {
        return new UserReview(reviewer, reviewee, score, comments, matchHistory);
    }
}
