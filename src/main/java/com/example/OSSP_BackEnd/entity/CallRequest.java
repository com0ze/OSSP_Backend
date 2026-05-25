package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "call_requests")
public class CallRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String itemName;

    @Column(nullable = false, length = 100)
    private String buildingName;

    @Column(nullable = false)
    private Integer rewardAmt;

    @Column(nullable = false)
    private Integer duration;

    @Column(length = 500)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.WAITING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MatchHistory matchHistory;


    public void markAsMatched() {
        this.status = RequestStatus.MATCHED;
    }

    public void markAsCanceled() {
        this.status = RequestStatus.CANCELED;
    }

    public void markAsInUse() {
        this.status = RequestStatus.IN_USE;
    }

    public void markAsCompleted() {
        this.status = RequestStatus.COMPLETED;
    }
}