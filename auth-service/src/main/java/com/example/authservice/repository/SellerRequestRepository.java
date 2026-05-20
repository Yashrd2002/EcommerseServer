package com.example.authservice.repository;

import com.example.authservice.model.SellerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRequestRepository extends JpaRepository<SellerRequest, Long> {
    
    Optional<SellerRequest> findByUserId(Long userId);
    
    List<SellerRequest> findByStatus(SellerRequest.RequestStatus status);
}