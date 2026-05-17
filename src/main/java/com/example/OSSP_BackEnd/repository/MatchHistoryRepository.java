package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchHistoryRepository extends JpaRepository<MatchHistory, Long> {

    @Query("SELECT mh FROM MatchHistory mh JOIN FETCH mh.request WHERE mh.request.id = :requestId")
    Optional<MatchHistory> findByRequestIdWithRequest(Long requestId);

    // 기존 메서드 유지
    Optional<MatchHistory> findByRequest_Id(Long requestId);
}
