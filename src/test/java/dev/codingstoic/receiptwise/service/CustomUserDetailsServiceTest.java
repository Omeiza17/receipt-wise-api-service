package dev.codingstoic.receiptwise.service;

import dev.codingstoic.receiptwise.model.User;
import dev.codingstoic.receiptwise.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserFoundByUsername() {
        String username = "testUser";
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername(username);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("hashedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("USER")));
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserFoundByEmail() {
        String emailAsUsername = "test@example.com";
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("actualUsername");
        mockUser.setEmail(emailAsUsername);
        mockUser.setPassword("hashedPassword");

        when(userRepository.findByUsername(emailAsUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(emailAsUsername)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(emailAsUsername);

        assertNotNull(userDetails);
        assertEquals("actualUsername", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("USER")));
        verify(userRepository).findByUsername(emailAsUsername);
        verify(userRepository).findByEmail(emailAsUsername);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        String usernameOrEmail = "nonExistentUser";
        when(userRepository.findByUsername(usernameOrEmail)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(usernameOrEmail)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(usernameOrEmail));

        assertEquals("User not found with username or email: " + usernameOrEmail, exception.getMessage());
        verify(userRepository).findByUsername(usernameOrEmail);
        verify(userRepository).findByEmail(usernameOrEmail);
    }
}