package dev.codingstoic.receiptwise.service;

import dev.codingstoic.receiptwise.dto.AuthResponse;
import dev.codingstoic.receiptwise.dto.LoginRequest;
import dev.codingstoic.receiptwise.dto.RegisterRequest;
import dev.codingstoic.receiptwise.model.User;
import dev.codingstoic.receiptwise.repository.UserRepository;
import dev.codingstoic.receiptwise.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        var request = new RegisterRequest("newUser", "new@example.com", "p@ssw0rd");

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashPassword");


        User savedUser = new User();
        savedUser.setUsername(request.username());
        savedUser.setEmail(request.email());
        savedUser.setPassword("hashPassword");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(request);

        assertNotNull(result);
        assertEquals(request.username(), result.getUsername());
        assertEquals(request.email(), result.getEmail());
        assertEquals("hashPassword", result.getPassword());

        verify(userRepository).save(any(User.class));

    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        var request = new RegisterRequest("existingUser", "new@example.com", "p@ssw0rd");

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.register(request), "User already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfullyWithUsername() {
        var request = new LoginRequest("user123", "p@ssw0rd");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(request.usernameOrEmail());
        user.setEmail("user123@example.com");

        when(userRepository.findByUsername(request.usernameOrEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(request.usernameOrEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("user123", "p@ssw0rd"));

        assertEquals("jwt-token", response.token());
        assertEquals("user123", response.username());
        assertEquals("user123@example.com", response.email());
    }

    @Test
    void shouldLoginSuccessfullyWithEmail() {
        var request = new LoginRequest("email@example.com", "p@ssw0rd");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user456");
        user.setEmail("email@example.com");

        when(userRepository.findByUsername("email@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user456")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("email@example.com", "p@ssw0rd"));

        assertEquals("jwt-token", response.token());
        assertEquals("user456", response.username());
        assertEquals("email@example.com", response.email());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        var request = new LoginRequest("nonexistentUser", "p@ssw0rd");

        when(userRepository.findByUsername("nonexistentUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistentUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request), "User not found with username or email: nonexistentUser");
    }

}
