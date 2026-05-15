package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {

    @Query("SELECT cr FROM CallRequest cr JOIN FETCH cr.requester WHERE cr.status = :status ORDER BY cr.createdAt DESC")
    List<CallRequest> findByStatusWithRequesterOrderByCreatedAtDesc(RequestStatus status);

    @Query("SELECT cr FROM CallRequest cr JOIN FETCH cr.requester ORDER BY cr.createdAt DESC")
    List<CallRequest> findAllWithRequesterOrderByCreatedAtDesc();

    @Query("SELECT cr FROM CallRequest cr JOIN FETCH cr.requester WHERE cr.requestId = :requestId")
    Optional<CallRequest> findByIdWithRequester(Long requestId);

    // 기존 메서드는 유지하거나 필요에 따라 수정
    List<CallRequest> findAllByOrderByCreatedAtDesc();
    List<CallRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
}
