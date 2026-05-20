
package com.example.authservice.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.authservice.dto.AddressRequest;
import com.example.authservice.dto.AddressResponse;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.OtpVerificationRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.SellerRequestDto;
import com.example.authservice.dto.UserResponse;
import com.example.authservice.exceptions.APIException;
import com.example.authservice.model.SellerRequest;
import com.example.authservice.model.User;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.service.AddressService;
import com.example.authservice.service.AuthService;
import com.example.authservice.service.SellerRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;
    
    private final AddressService addressService;
    
    private final SellerRequestService sellerRequestService;
    @PostMapping("/secure/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new APIException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new APIException("Email already exists");
        }

        String otp = authService.generateOtp();
        authService.sendOtpEmail(request.getEmail(), otp);
        authService.storeOtpAndUser(request, otp);

        return ResponseEntity.ok("Welcome to E-Commerce Hub! An OTP has been sent to your email. Please verify to complete registration.");
    }


    @PostMapping("/secure/verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        boolean verified = authService.verifyOtp(request.getEmail(), request.getOtp());
        if (verified) {
            return ResponseEntity.ok("User registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }
    }

    @PostMapping("/secure/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUser")
    public ResponseEntity<UserResponse> getUser(@RequestHeader("X-User-Id") Long userId) {
        User user = authService.getUserById(userId);

        UserResponse response = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/getUser/{userId}")
    public ResponseEntity<UserResponse> getUserforSeller(@PathVariable Long userId) {
        User user = authService.getUserById(userId);

        UserResponse response = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/getAllSellers")
    public ResponseEntity<List<UserResponse>> getAllSellers() {
        List<UserResponse> sellers = authService.getAllSellers();
        return ResponseEntity.ok(sellers);
    }

    
    @PostMapping("/address/add")
    public ResponseEntity<String> addAddress(@Valid @RequestBody AddressRequest request,
                                             @RequestHeader("X-User-Id") Long userId) {
        addressService.addAddress(request, userId);
        return ResponseEntity.ok("Address added successfully");
    }

    @PutMapping("/address/edit/{id}")
    public ResponseEntity<String> editAddress(@Valid @RequestBody AddressRequest request,
                                              @RequestHeader("X-User-Id") Long userId,
                                              @PathVariable Long id) {
        addressService.editAddress(request, userId, id);
        return ResponseEntity.ok("Address edited successfully");
    }

    @GetMapping("/address/list")
    public ResponseEntity<List<AddressResponse>> getAddresses(@RequestHeader("X-User-Id") Long userId) {
        List<AddressResponse> addresses = addressService.getAddresses(userId);
        return ResponseEntity.ok(addresses);
    }
    
    @GetMapping("/address/{id}")
    public ResponseEntity<AddressResponse> getAddressById(@PathVariable Long id) {
        AddressResponse address = addressService.getAddressById(id);
        return ResponseEntity.ok(address);
    }
    @DeleteMapping("/address/delete/{id}")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-User-Id") Long userId,
                                                @PathVariable Long id) {
        addressService.deleteAddress(userId, id);
        return ResponseEntity.ok("Address deleted successfully");
    }

    @PostMapping("/seller/request")
    public ResponseEntity<String> submitSellerRequest(@Valid @RequestBody SellerRequestDto requestDto) {
    	System.out.print("hi"+requestDto.getUserId());
        sellerRequestService.submitSellerRequest(requestDto.getUserId(), requestDto);
        return ResponseEntity.ok("Seller request submitted successfully");
    }

    @GetMapping("/admin/seller-requests")
    public ResponseEntity<List<SellerRequest>> getPendingSellerRequests() {
        List<SellerRequest> requests = sellerRequestService.getPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/admin/seller-requests/{requestId}/approve")
    public ResponseEntity<String> approveSellerRequest(@PathVariable Long requestId,
                                                       @RequestParam(required = false) String comments) {
        sellerRequestService.approveSellerRequest(requestId, comments);
        return ResponseEntity.ok("Seller request approved successfully");
    }

    @PutMapping("/admin/seller-requests/{requestId}/reject")
    public ResponseEntity<String> rejectSellerRequest(@PathVariable Long requestId,
                                                      @RequestParam(required = false) String comments) {
        sellerRequestService.rejectSellerRequest(requestId, comments);
        return ResponseEntity.ok("Seller request rejected");
    }

}
