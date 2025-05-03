package dev.codingstoic.receiptwise.dto;

public record AuthResponse(
        String userId,
        String username,
        String email,
        String token
) {
}
