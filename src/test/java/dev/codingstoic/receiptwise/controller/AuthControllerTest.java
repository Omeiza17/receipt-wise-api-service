package dev.codingstoic.receiptwise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.codingstoic.receiptwise.dto.AuthResponse;
import dev.codingstoic.receiptwise.dto.LoginRequest;
import dev.codingstoic.receiptwise.dto.RegisterRequest;
import dev.codingstoic.receiptwise.model.User;
import dev.codingstoic.receiptwise.security.JwtService;
import dev.codingstoic.receiptwise.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void registerShouldReturnCreatedWithLocation() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User();
        user.setId(uuid);
        user.setUsername("testUser");
        user.setEmail("test@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("email", "test@example.com")
                        .param("password", "password123"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/user/%s".formatted(uuid.toString())));

    }

    @Test
    void login_ShouldReturn200WithAuthResponse() throws Exception {
        AuthResponse authResponse = new AuthResponse("42", "testUser", "test@example.com", "fake-jwt-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("usernameOrEmail", "testUser")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("42"))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }
}