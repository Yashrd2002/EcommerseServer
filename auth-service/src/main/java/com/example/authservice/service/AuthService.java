package com.example.authservice.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.UserResponse;
import com.example.authservice.exceptions.APIException;
import com.example.authservice.exceptions.ResourceNotFoundException;
import com.example.authservice.model.OtpVerification;
import com.example.authservice.model.Role;
import com.example.authservice.model.User;
import com.example.authservice.repository.OtpRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private OtpRepository otpRepository;


	  private static final Random RANDOM = new Random();
	    public String generateOtp() {
	        return String.valueOf(RANDOM.nextInt(900000) + 100000);
	    }

	public void sendOtpEmail(String toEmail, String otp) {
	    SimpleMailMessage message = new SimpleMailMessage();
	    message.setTo(toEmail);
	    message.setSubject("Welcome to E-Commerce Hub – Verify Your Email");

	    String emailBody = String.format(
	        "Hi there!\n\n" +
	        "Welcome to E-Commerce Hub – your one-stop destination for everything you love to shop!\n\n" +
	        "To complete your registration, please use the following One-Time Password (OTP):\n\n" +
	        "🔐 OTP: %s\n\n" +
	        "This OTP is valid for 5 minutes. Please do not share it with anyone.\n\n" +
	        "We're excited to have you on board. Happy shopping!\n\n" +
	        "Warm regards,\n" +
	        "E-Commerce Hub Team", otp
	    );

	    message.setText(emailBody);
	    mailSender.send(message);
	}

    public void storeOtpAndUser(RegisterRequest request, String otp) {
        OtpVerification otpVerification = OtpVerification.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(request.getRole().name())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        otpRepository.save(otpVerification);
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<OtpVerification> optionalRecord = otpRepository.findTopByEmailOrderByExpiryTimeDesc(email);

        if (optionalRecord.isEmpty()) {
            throw new APIException("OTP record not found for email: " + email);
        }

        OtpVerification record = optionalRecord.get();

        boolean isExpired = record.getExpiryTime().isBefore(LocalDateTime.now());
        boolean isInvalidOtp = !record.getOtp().equals(otp);

        if (isExpired || isInvalidOtp) {
            otpRepository.delete(record);
            return false;
        }

        try {
            User user = User.builder()
                    .username(record.getUsername())
                    .password(passwordEncoder.encode(record.getPassword()))
                    .role(Role.valueOf(record.getRole().toUpperCase()))
                    .email(record.getEmail())
                    .build();

            userRepository.save(user);
            otpRepository.delete(record);
            return true;
        } catch (IllegalArgumentException e) {
            throw new APIException("Invalid role value: " + record.getRole());
        }
    }

	public AuthResponse login(LoginRequest request) {
	    User user = userRepository.findByUsername(request.getUsername())
	            .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new APIException("Invalid credentials");
	    }

	    String token = jwtUtil.generateToken(user.getUsername(), user.getId(),user.getRole().name());
	    return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
	}

	public User getUser(String username) {
	    return userRepository.findByUsername(username)
	            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
	}

	public User getUserById(Long userId) {
	    return userRepository.findById(userId)
	            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
	}

	public List<UserResponse> getAllSellers() {
	    List<User> sellers = userRepository.findByRole(Role.SELLER);
	    
	    return sellers.stream()
	            .map(user -> new UserResponse(
	                    user.getId(),
	                    user.getUsername(),
	                    user.getRole(),
	                    user.getEmail()
	            ))
	            .collect(Collectors.toList());
	}


	
}