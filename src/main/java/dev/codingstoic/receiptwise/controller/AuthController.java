package dev.codingstoic.receiptwise.controller;

import dev.codingstoic.receiptwise.dto.LoginRequest;
import dev.codingstoic.receiptwise.dto.RegisterRequest;
import dev.codingstoic.receiptwise.dto.AuthResponse;
import dev.codingstoic.receiptwise.model.User;
import dev.codingstoic.receiptwise.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@ModelAttribute RegisterRequest request) {
        User user = authService.register(request);

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
        return ResponseEntity.ok(authService.login(request));
    }
}
