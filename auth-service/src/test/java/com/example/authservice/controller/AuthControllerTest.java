package com.example.authservice.controller;

import com.example.authservice.dto.*;

import com.example.authservice.model.User;
import com.example.authservice.service.AddressService;
import com.example.authservice.service.AuthService;
import com.example.authservice.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private UserRepository userRepository;
    @MockBean private AddressService addressService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setup() {
        registerRequest = new RegisterRequest("user", "pass", Role.USER, "email@test.com");
    }

    @Test
    void register_ShouldReturnSuccessMessage() throws Exception {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("email@test.com")).thenReturn(false);
        when(authService.generateOtp()).thenReturn("123456");

        mockMvc.perform(post("/auth/secure/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Welcome")));
    }

    @Test
    void register_ShouldThrow_WhenUsernameExists() throws Exception {
        when(userRepository.existsByUsername("user")).thenReturn(true);

        mockMvc.perform(post("/auth/secure/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldThrow_WhenEmailExists() throws Exception {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("email@test.com")).thenReturn(true);

        mockMvc.perform(post("/auth/secure/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyOtp_ShouldReturnSuccess_WhenValid() throws Exception {
        OtpVerificationRequest otpReq = new OtpVerificationRequest("email@test.com", "123456");
        when(authService.verifyOtp("email@test.com", "123456")).thenReturn(true);

        mockMvc.perform(post("/auth/secure/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpReq)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void verifyOtp_ShouldReturnBadRequest_WhenInvalid() throws Exception {
        OtpVerificationRequest otpReq = new OtpVerificationRequest("email@test.com", "999999");
        when(authService.verifyOtp("email@test.com", "999999")).thenReturn(false);

        mockMvc.perform(post("/auth/secure/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpReq)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired OTP"));
    }

    @Test
    void login_ShouldReturnAuthResponse() throws Exception {
        LoginRequest login = new LoginRequest("user", "pass");
        AuthResponse response = new AuthResponse("token", 1L, "user", "USER");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/secure/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void getUser_ShouldReturnUserResponse() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setEmail("email@test.com");
        user.setRole(com.example.authservice.model.Role.USER);

        when(authService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/auth/getUser").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("email@test.com"));
    }

    @Test
    void getAllSellers_ShouldReturnList() throws Exception {
        UserResponse seller = new UserResponse(1L, "seller", com.example.authservice.model.Role.SELLER, "s@e.com");
        when(authService.getAllSellers()).thenReturn(List.of(seller));

        mockMvc.perform(get("/auth/admin/getAllSellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("seller"));
    }

    @Test
    void addAddress_ShouldReturnSuccess() throws Exception {
        AddressRequest req = new AddressRequest("Street", "City", "State", "12345", "Country");

        mockMvc.perform(post("/auth/address/add")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Address added successfully"));

        verify(addressService).addAddress(any(AddressRequest.class), eq(1L));
    }
}
