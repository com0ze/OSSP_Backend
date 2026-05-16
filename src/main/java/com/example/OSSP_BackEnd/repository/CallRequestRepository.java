//DB에 직접 붙어서 데이터를 저장하고(save), 꺼내오는(find) 역할

package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.CallStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {
    
    // status가 WAITING인 요청들을 최신순으로 조회
    List<CallRequest> findByStatusOrderByCreatedAtDesc(CallStatus status);
}
