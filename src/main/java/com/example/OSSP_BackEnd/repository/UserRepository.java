package com.example.OSSP_BackEnd.repository;

import com.example.OSSP_BackEnd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}