package com.example.authservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String businessName;
    
    @Column(nullable = false)
    private String businessType;
    

    
    @Column(nullable = false)
    private String businessAddress;
    
    @Column(nullable = false)
    private String contactNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();
    
    private LocalDateTime approvedDate;
    
    private String adminComments;
    
    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }
}