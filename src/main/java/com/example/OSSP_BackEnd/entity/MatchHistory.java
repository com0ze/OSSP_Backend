package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_history")
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Long id;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private CallRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private User provider;

    private MatchHistory(CallRequest request, User provider) {
        this.request = request;
        this.provider = provider;
        this.matchedAt = LocalDateTime.now();
    }

    public static MatchHistory create(CallRequest request, User provider) {
        return new MatchHistory(request, provider);
    }

    public void markAsReturned(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }
}