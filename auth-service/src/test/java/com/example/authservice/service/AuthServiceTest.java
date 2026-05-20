package com.example.authservice.service;

import com.example.authservice.dto.*;
import com.example.authservice.dto.Role;
import com.example.authservice.exceptions.APIException;
import com.example.authservice.exceptions.ResourceNotFoundException;
import com.example.authservice.model.*;
import com.example.authservice.repository.*;
import com.example.authservice.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Full test coverage for AuthService.
 */
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private JavaMailSender mailSender;
    @Mock private OtpRepository otpRepository;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        // manually inject field-based dependencies
        ReflectionTestUtils.setField(authService, "otpRepository", otpRepository);
        ReflectionTestUtils.setField(authService, "mailSender", mailSender);
    }


    // ✅ generateOtp
    @Test
    void generateOtp_ShouldReturnSixDigitString() {
        String otp = authService.generateOtp();
        assertThat(otp).matches("\\d{6}");
    }

    // ✅ sendOtpEmail
    @Test
    void sendOtpEmail_ShouldSendMailSuccessfully() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        authService.sendOtpEmail("test@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).contains("test@example.com");
        assertThat(sent.getSubject()).contains("Verify Your Email");
        assertThat(sent.getText()).contains("123456");
    }

    // ✅ storeOtpAndUser
    @Test
    void storeOtpAndUser_ShouldSaveOtpVerification() {
        RegisterRequest req = new RegisterRequest("user", "pass", Role.USER, "email@test.com");

        authService.storeOtpAndUser(req, "654321");

        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(captor.capture());

        OtpVerification record = captor.getValue();
        assertThat(record.getEmail()).isEqualTo("email@test.com");
        assertThat(record.getOtp()).isEqualTo("654321");
        assertThat(record.getExpiryTime()).isAfter(LocalDateTime.now());
    }

    // ✅ verifyOtp success
    @Test
    void verifyOtp_ShouldCreateUser_WhenOtpIsValid() {
        OtpVerification record = OtpVerification.builder()
                .email("user@test.com")
                .username("user")
                .password("pass")
                .role("USER")
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(3))
                .build();

        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc("user@test.com"))
                .thenReturn(Optional.of(record));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        boolean result = authService.verifyOtp("user@test.com", "123456");

        assertThat(result).isTrue();
        verify(userRepository).save(any(User.class));
        verify(otpRepository).delete(record);
    }

    // ✅ verifyOtp invalid OTP
    @Test
    void verifyOtp_ShouldReturnFalse_WhenOtpDoesNotMatch() {
        OtpVerification record = OtpVerification.builder()
                .email("user@test.com")
                .otp("999999")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc("user@test.com"))
                .thenReturn(Optional.of(record));

        boolean result = authService.verifyOtp("user@test.com", "123456");

        assertThat(result).isFalse();
        verify(otpRepository).delete(record);
    }

    // ✅ verifyOtp expired
    @Test
    void verifyOtp_ShouldReturnFalse_WhenExpired() {
        OtpVerification record = OtpVerification.builder()
                .email("user@test.com")
                .otp("123456")
                .expiryTime(LocalDateTime.now().minusMinutes(2))
                .build();

        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc("user@test.com"))
                .thenReturn(Optional.of(record));

        boolean result = authService.verifyOtp("user@test.com", "123456");

        assertThat(result).isFalse();
        verify(otpRepository).delete(record);
    }

    // ✅ verifyOtp missing record
    @Test
    void verifyOtp_ShouldThrow_WhenRecordMissing() {
        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc("missing@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyOtp("missing@test.com", "000000"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("OTP record not found");
    }

    // ✅ verifyOtp invalid role exception
    @Test
    void verifyOtp_ShouldThrow_WhenRoleIsInvalid() {
        OtpVerification record = OtpVerification.builder()
                .email("user@test.com")
                .username("user")
                .password("pass")
                .role("INVALID_ROLE")
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc("user@test.com"))
                .thenReturn(Optional.of(record));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        assertThatThrownBy(() -> authService.verifyOtp("user@test.com", "123456"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Invalid role value");
    }

    // ✅ login success
    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsValid() {
        LoginRequest req = new LoginRequest("user", "pass");
        User user = User.builder()
                .id(1L)
                .username("user")
                .password("encoded")
                .role(com.example.authservice.model.Role.USER)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token123");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getUsername()).isEqualTo("user");
    }

    // ✅ login invalid password
    @Test
    void login_ShouldThrow_WhenPasswordMismatch() {
        LoginRequest req = new LoginRequest("user", "wrong");
        User user = User.builder()
                .id(1L)
                .username("user")
                .password("encoded")
                .role(com.example.authservice.model.Role.USER)
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Invalid credentials");
    }

    // ✅ login missing user
    @Test
    void login_ShouldThrow_WhenUserNotFound() {
        LoginRequest req = new LoginRequest("ghost", "123");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    // ✅ getUserById success
    @Test
    void getUserById_ShouldReturnUser_WhenFound() {
        User user = User.builder().id(1L).username("john").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = authService.getUserById(1L);
        assertThat(result.getUsername()).isEqualTo("john");
    }

    // ✅ getUserById not found
    @Test
    void getUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.getUserById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ✅ getUser success
    @Test
    void getUser_ShouldReturnUser_WhenFound() {
        User user = User.builder().id(2L).username("alice").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User found = authService.getUser("alice");
        assertThat(found.getId()).isEqualTo(2L);
    }

    // ✅ getUser not found
    @Test
    void getUser_ShouldThrow_WhenMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.getUser("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ✅ getAllSellers
    @Test
    void getAllSellers_ShouldMapUserResponsesCorrectly() {
        List<User> sellers = List.of(
                User.builder().id(1L).username("seller1").email("s1@e.com").role(com.example.authservice.model.Role.SELLER).build(),
                User.builder().id(2L).username("seller2").email("s2@e.com").role(com.example.authservice.model.Role.SELLER).build()
        );

        when(userRepository.findByRole(com.example.authservice.model.Role.SELLER)).thenReturn(sellers);

        List<UserResponse> result = authService.getAllSellers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("seller1");
    }
}
