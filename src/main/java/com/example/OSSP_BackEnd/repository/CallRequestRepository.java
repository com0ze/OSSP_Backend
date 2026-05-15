package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.CallRequest;
import com.example.OSSP_BackEnd.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {

    List<CallRequest> findAllByOrderByCreatedAtDesc();

    List<CallRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
}