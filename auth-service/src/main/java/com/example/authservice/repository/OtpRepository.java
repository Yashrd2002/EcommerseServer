package com.example.authservice.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.authservice.model.OtpVerification;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailOrderByExpiryTimeDesc(String email);
}