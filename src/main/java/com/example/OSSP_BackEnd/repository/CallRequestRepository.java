package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {

    /*
     상태별 대여 요청 최신순 조회 (N+1 방지 패치 조인 적용)
     */
    @Query("SELECT cr FROM CallRequest cr JOIN FETCH cr.requester WHERE cr.status = :status ORDER BY cr.createdAt DESC")
    List<CallRequest> findByStatusWithRequesterOrderByCreatedAtDesc(@Param("status") RequestStatus status);

    /*
     모든 대여 요청 최신순 조회 (N+1 방지 패치 조인 적용)
     */
    @Query("SELECT cr FROM CallRequest cr JOIN FETCH cr.requester ORDER BY cr.createdAt DESC")
    List<CallRequest> findAllWithRequesterOrderByCreatedAtDesc();

    /*
     단건 상세 조회 (N+1 방지 패치 조인 적용)
     */
    @Query("SELECT cr FROM CallRequest cr JOIN FETCH cr.requester WHERE cr.id = :requestId")
    Optional<CallRequest> findByIdWithRequester(@Param("requestId") Long requestId);

    /*
     에넘 타입을 RequestStatus로 일치화
     */
    List<CallRequest> findAllByOrderByCreatedAtDesc();

    /*
     특정 유저의 요청 중 상태 리스트에 포함되는 것들을 최신순으로 조회 (N+1 방지 패치 조인 적용)
     */
    @Query("SELECT cr FROM CallRequest cr JOIN FETCH cr.requester WHERE cr.requester.id = :userId AND cr.status IN :statuses ORDER BY cr.createdAt DESC")
    List<CallRequest> findByRequesterIdAndStatusInOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("statuses") List<RequestStatus> statuses);
    
    /**
     * 💡 [수정됨] 도배 방지: 단순 문자열('WAITING') 대신 Enum 타입을 파라미터로 받도록 수정
     * @param userId 수요자 ID
     * @param status 요청 상태 (Enum)
     * @return 해당 상태의 요청이 있으면 true
     */
    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM CallRequest cr WHERE cr.requester.id = :userId AND cr.status = :status")
    boolean hasWaitingRequest(@Param("userId") Long userId, @Param("status") RequestStatus status);
}