package dev.codingstoic.receiptwise.controller;

import dev.codingstoic.receiptwise.dto.LoginRequest;
import dev.codingstoic.receiptwise.dto.RegisterRequest;
import dev.codingstoic.receiptwise.dto.AuthResponse;
import dev.codingstoic.receiptwise.model.User;
import dev.codingstoic.receiptwise.repository.UserRepository;
import dev.codingstoic.receiptwise.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@ModelAttribute RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        log.info("AuthController | register | Registered user: {}", user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/user/{id}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@ModelAttribute LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password()));
        User user = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + request.usernameOrEmail()));
        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(user.getId().toString(), user.getUsername(), user.getEmail(), token));
    }
}
