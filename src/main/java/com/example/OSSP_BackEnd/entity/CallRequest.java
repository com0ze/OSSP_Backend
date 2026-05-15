package com.example.OSSP_BackEnd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "call_request")
@EntityListeners(AuditingEntityListener.class)
public class CallRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "item_name", length = 100)
    private String itemName;

    @Column(name = "building_name", length = 100)
    private String buildingName;

    @Column(name = "reward_amt", length = 50)
    private String rewardAmt;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "memo", length = 255)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RequestStatus status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    private CallRequest(
            String itemName,
            String buildingName,
            String rewardAmt,
            String duration,
            String memo,
            User requester
    ) {
        this.itemName = itemName;
        this.buildingName = buildingName;
        this.rewardAmt = rewardAmt;
        this.duration = duration;
        this.memo = memo;
        this.status = RequestStatus.WAITING;
        this.requester = requester;
    }

    public static CallRequest create(
            String itemName,
            String buildingName,
            String rewardAmt,
            String duration,
            String memo,
            User requester
    ) {
        return new CallRequest(itemName, buildingName, rewardAmt, duration, memo, requester);
    }

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