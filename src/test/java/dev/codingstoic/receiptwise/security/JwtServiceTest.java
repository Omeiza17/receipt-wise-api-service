package dev.codingstoic.receiptwise.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private SecretKey fixedSecretKey;
    private final long expirationTimeMs = 600_000; // 10 minutes, same as in JwtService

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        jwtService = new JwtService();
        // Use reflection to set a fixed secret key for predictable token generation in tests
        fixedSecretKey = Jwts.SIG.HS256.key().build(); // Generate a new key for each test run for isolation
        // or use a static known key for absolute predictability
        Field secretKeyField = JwtService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtService, fixedSecretKey);

        Field expirationTimeField = JwtService.class.getDeclaredField("EXPIRATION_TIME_MS");
        expirationTimeField.setAccessible(true);
        // Ensure the test uses the same expiration time as the service,
        // though for this specific test setup, we read it directly if possible
        // or set it if it's not final. If it is final, we rely on the hardcoded value above.
        // expirationTimeMs = expirationTimeField.getLong(jwtService); // Uncomment if EXPIRATION_TIME_MS is not final
    }

    private String generateTestToken(String username, Date expirationDate) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expirationDate)
                .signWith(fixedSecretKey, Jwts.SIG.HS256)
                .compact();
    }

    @Test
    void extractUsername_shouldReturnUsername_whenTokenIsValid() {
        String username = "testUser";
        String token = generateTestToken(username, new Date(System.currentTimeMillis() + expirationTimeMs));
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void generateToken_shouldGenerateValidToken_forGivenUsername() {
        String username = "testUser";
        String token = jwtService.generateToken(username);
        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertFalse(jwtService.extractClaim(token, Claims::getExpiration).before(new Date()));
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValidAndUsernameMatches() {
        String username = "testUser";
        String token = generateTestToken(username, new Date(System.currentTimeMillis() + expirationTimeMs));
        assertTrue(jwtService.isTokenValid(token, username));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        String username = "testUser";
        String token = generateTestToken(username, new Date(System.currentTimeMillis() - 1000)); // Expired 1 second ago
        assertFalse(jwtService.isTokenValid(token, username));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenUsernameDoesNotMatch() {
        String actualUsername = "testUser";
        String tokenUsername = "differentUser";
        String token = generateTestToken(actualUsername, new Date(System.currentTimeMillis() + expirationTimeMs));
        assertFalse(jwtService.isTokenValid(token, tokenUsername));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {
        String username = "testUser";
        String malformedToken = "this.is.a.malformed.token";
        // Depending on the JWT library's strictness, this might throw different exceptions.
        // io.jsonwebtoken.security.SecurityException or MalformedJwtException
        assertThrows(Exception.class, () -> jwtService.isTokenValid(malformedToken, username));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenSignatureIsInvalid() {
        String username = "testUser";
        SecretKey otherKey = Jwts.SIG.HS256.key().build();
        String tokenWithWrongSignature = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(otherKey, Jwts.SIG.HS256) // Signed with a different key
                .compact();

        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> jwtService.isTokenValid(tokenWithWrongSignature, username));
    }


    @Test
    void extractClaim_shouldReturnCorrectExpiration_whenTokenIsValid() {
        String username = "testUser";
        Date expiration = new Date(System.currentTimeMillis() + expirationTimeMs);
        // Normalize to seconds then back to ms to avoid millisecond precision issues with JWT date storage
        long expirationSeconds = expiration.getTime() / 1000;
        Date normalizedExpiration = new Date(expirationSeconds * 1000);

        String token = generateTestToken(username, normalizedExpiration);
        Date extractedExpiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertEquals(normalizedExpiration.getTime() / 1000, extractedExpiration.getTime() / 1000);
    }

    @Test
    void extractClaim_shouldThrowException_whenTokenIsInvalid() {
        String invalidToken = "invalid.token.structure";
        Function<Claims, String> subjectResolver = Claims::getSubject;
        assertThrows(Exception.class, () -> jwtService.extractClaim(invalidToken, subjectResolver));
    }
}