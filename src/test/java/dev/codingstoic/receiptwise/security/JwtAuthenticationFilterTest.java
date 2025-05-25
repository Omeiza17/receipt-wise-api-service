package dev.codingstoic.receiptwise.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext(); // Ensure clean security context for each test
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic e21e83-u285b92-289f2");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldAuthenticate_whenJwtIsValidAndUserExists() throws ServletException, IOException {
        String jwt = "valid.jwt.token";
        String username = "testUser";
        UserDetails userDetails = new User(username, "password", new ArrayList<>());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, username)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenJwtServiceReportsTokenAsInvalidForUser() throws ServletException, IOException {
        String jwt = "valid.jwt.but.mismatched.user";
        String usernameFromToken = "userA";
        UserDetails userDetailsForUserA = new User(usernameFromToken, "password", new ArrayList<>());

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(usernameFromToken);
        when(userDetailsService.loadUserByUsername(usernameFromToken)).thenReturn(userDetailsForUserA);
        when(jwtService.isTokenValid(jwt, usernameFromToken)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenUsernameFromTokenIsNull() throws ServletException, IOException {
        String jwt = "jwt.without.username";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void doFilterInternal_shouldNotAuthenticate_whenUserDetailsServiceThrowsException() throws ServletException, IOException {
        String jwt = "jwt.for.nonexistent.user";
        String username = "ghostUser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException("User not found"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).isTokenValid(anyString(), anyString());
    }


    @Test
    void doFilterInternal_shouldPassThrough_whenAuthenticationAlreadyExists() throws ServletException, IOException {
        // Simulate an already authenticated context
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alreadyAuthedUser", null, new ArrayList<>()));

        String jwt = "any.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(jwt);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(eq(jwt), anyString()); // isTokenValid should not be called

        // Assert that the original authentication is still there
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("alreadyAuthedUser", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}