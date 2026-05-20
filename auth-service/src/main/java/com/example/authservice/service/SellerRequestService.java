package com.example.authservice.service;

import com.example.authservice.dto.SellerRequestDto;
import com.example.authservice.exceptions.APIException;
import com.example.authservice.exceptions.ResourceNotFoundException;
import com.example.authservice.model.Role;
import com.example.authservice.model.SellerRequest;
import com.example.authservice.model.User;
import com.example.authservice.repository.SellerRequestRepository;
import com.example.authservice.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.System.Logger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SellerRequestService {
    
    @Autowired
    private SellerRequestRepository sellerRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    


    @Transactional
    public SellerRequest submitSellerRequest(Long userId, SellerRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        sellerRequestRepository.findByUserId(userId).ifPresent(sr -> {
            throw new APIException("Seller request already exists for this user");
        });

        SellerRequest sellerRequest = SellerRequest.builder()
                .userId(userId)
                .businessName(requestDto.getBusinessName())
                .businessType(requestDto.getBusinessType())
                .businessAddress(requestDto.getBusinessAddress())
                .contactNumber(requestDto.getContactNumber())
                .status(SellerRequest.RequestStatus.PENDING)     // explicit
                .requestDate(LocalDateTime.now())                 // explicit
                .build();

        SellerRequest saved = sellerRequestRepository.save(sellerRequest);
        return saved;
    }

    
    public List<SellerRequest> getPendingRequests() {
        return sellerRequestRepository.findByStatus(SellerRequest.RequestStatus.PENDING);
    }
    
    public SellerRequest approveSellerRequest(Long requestId, String adminComments) {
        SellerRequest request = sellerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerRequest", "id", requestId));
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));
        
        user.setRole(Role.SELLER);
        userRepository.save(user);
        
        request.setStatus(SellerRequest.RequestStatus.APPROVED);
        request.setApprovedDate(LocalDateTime.now());
        request.setAdminComments(adminComments);
        
        return sellerRequestRepository.save(request);
    }
    
    public SellerRequest rejectSellerRequest(Long requestId, String adminComments) {
        SellerRequest request = sellerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("SellerRequest", "id", requestId));
        
        request.setStatus(SellerRequest.RequestStatus.REJECTED);
        request.setAdminComments(adminComments);
        
        return sellerRequestRepository.save(request);
    }
}