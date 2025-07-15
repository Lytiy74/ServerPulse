package ua.azaika.serverpulse.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import ua.azaika.serverpulse.entity.Role;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.exception.JwtTokenGenerationException;
import ua.azaika.serverpulse.exception.JwtTokenValidationException;
import ua.azaika.serverpulse.security.CustomUserDetails;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private final String testSecretKey = Base64.getEncoder().encodeToString(
            "this_is_a_very_long_and_secure_secret_key_for_jwt_testing_purposes_1234567890abcdefghijklmnopqrstuvwxyz"
                    .getBytes(StandardCharsets.UTF_8));
    private final long testExpiration = 3600000; // 1 година

    private UserEntity testUserEntity;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

        testUserEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(List.of(Role.USER, Role.ADMIN))
                .createdAt(LocalDateTime.now())
                .build();
        testUserDetails = new CustomUserDetails(testUserEntity);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {
        @Test
        @DisplayName("Should generate a valid token for CustomUserDetails")
        void shouldGenerateValidTokenForCustomUserDetails() {
            String token = jwtService.generateToken(testUserDetails);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            assertDoesNotThrow(() -> jwtService.extractAllClaims(token));

            assertThat(jwtService.extractUsername(token)).contains(testUserEntity.getUsername());
            assertThat(jwtService.extractId(token)).contains(testUserEntity.getId().toString());
            assertThat(jwtService.extractRoles(token)).contains(List.of("ROLE_USER", "ROLE_ADMIN"));
        }

        @Test
        @DisplayName("Should throw JwtTokenGenerationException if UserEntity ID is null")
        void shouldThrowExceptionIfUserIdIsNull() {
            UserEntity userWithNullId = UserEntity.builder()
                    .username("no_id_user").email("no_id@example.com").roles(List.of(Role.USER)).build();
            CustomUserDetails customUserDetailsWithNullId = new CustomUserDetails(userWithNullId);

            assertThrows(JwtTokenGenerationException.class, () -> jwtService.generateToken(customUserDetailsWithNullId),
                    "Should throw exception if User ID is null");
        }

        @Test
        @DisplayName("Should throw JwtTokenGenerationException if UserDetails is not CustomUserDetails")
        void shouldThrowExceptionIfUserDetailsIsNotCustomUserDetails() {
            UserDetails anonymousUserDetails = org.springframework.security.core.userdetails.User.builder()
                    .username("anonymous")
                    .password("N/A")
                    .roles("ANONYMOUS")
                    .build();

            assertThrows(JwtTokenGenerationException.class, () -> jwtService.generateToken(anonymousUserDetails),
                    "Should throw exception if UserDetails is not CustomUserDetails");
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class TokenExtractionTests {
        private String validToken;
        private String tokenWithoutRoles;
        private String tokenWithoutId;
        private String tokenWithoutSubject;


        @BeforeEach
        void setupTokens() {
            validToken = jwtService.generateToken(testUserDetails);

            tokenWithoutRoles = Jwts.builder()
                    .subject(testUserEntity.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + testExpiration))
                    .signWith(jwtService.getSignInKey())
                    .compact();

            tokenWithoutId = Jwts.builder()
                    .subject(testUserEntity.getUsername())
                    .claim("roles", List.of("ROLE_USER"))
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + testExpiration))
                    .signWith(jwtService.getSignInKey())
                    .compact();

            tokenWithoutSubject = Jwts.builder()
                    .claim("roles", List.of("ROLE_USER"))
                    .claim("uuid", UUID.randomUUID().toString())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + testExpiration))
                    .signWith(jwtService.getSignInKey())
                    .compact();
        }

        @Test
        @DisplayName("Should extract username from valid token")
        void shouldExtractUsernameFromValidToken() {
            assertThat(jwtService.extractUsername(validToken)).contains(testUserEntity.getUsername());
        }

        @Test
        @DisplayName("Should extract roles from valid token")
        void shouldExtractRolesFromValidToken() {
            assertThat(jwtService.extractRoles(validToken)).contains(List.of("ROLE_USER", "ROLE_ADMIN"));
        }

        @Test
        @DisplayName("Should extract ID from valid token")
        void shouldExtractIdFromValidToken() {
            assertThat(jwtService.extractId(validToken)).contains(testUserEntity.getId().toString());
        }

        @Test
        @DisplayName("Should return empty Optional if username claim is missing")
        void shouldReturnEmptyOptionalIfUsernameMissing() {
            assertThat(jwtService.extractUsername(tokenWithoutSubject)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional if roles claim is missing")
        void shouldReturnEmptyOptionalIfRolesMissing() {
            assertThat(jwtService.extractRoles(tokenWithoutRoles)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty Optional if ID claim is missing")
        void shouldReturnEmptyOptionalIfIdMissing() {
            assertThat(jwtService.extractId(tokenWithoutId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {
        private String validToken;
        private String expiredToken;
        private String malformedToken;
        private String tokenWithInvalidSignature;

        @BeforeEach
        void setupValidationTokens() {
            validToken = jwtService.generateToken(testUserDetails);

            expiredToken = Jwts.builder()
                    .subject(testUserEntity.getUsername())
                    .claim("roles", List.of("ROLE_USER"))
                    .claim("uuid", testUserEntity.getId().toString())
                    .issuedAt(new Date(System.currentTimeMillis() - testExpiration - 1000))
                    .expiration(new Date(System.currentTimeMillis() - 1000))
                    .signWith(jwtService.getSignInKey())
                    .compact();

            malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE2MjMxMjM0NTZ9.malformed_signature"; // Некоректний формат

            Key anotherKey = Keys.hmacShaKeyFor("another_secret_key_which_is_different_from_the_test_secret".getBytes(StandardCharsets.UTF_8));
            tokenWithInvalidSignature = Jwts.builder()
                    .subject(testUserEntity.getUsername())
                    .claim("roles", List.of("ROLE_USER"))
                    .claim("uuid", testUserEntity.getId().toString())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + testExpiration))
                    .signWith(anotherKey, SignatureAlgorithm.HS256)
                    .compact();
        }

        @Test
        @DisplayName("Should return true for a valid token")
        void shouldReturnTrueForValidToken() {
            assertTrue(jwtService.isTokenValid(validToken));
        }

        @Test
        @DisplayName("Should throw JwtTokenValidationException for an expired token")
        void shouldThrowExceptionForExpiredToken() {
            assertThrows(JwtTokenValidationException.class, () -> jwtService.isTokenValid(expiredToken));
        }

        @Test
        @DisplayName("Should throw JwtTokenValidationException for a malformed token")
        void shouldThrowExceptionForMalformedToken() {
            assertThrows(JwtTokenValidationException.class, () -> jwtService.isTokenValid(malformedToken));
        }

        @Test
        @DisplayName("Should throw JwtTokenValidationException for a token with invalid signature")
        void shouldThrowExceptionForTokenWithInvalidSignature() {
            assertThrows(JwtTokenValidationException.class, () -> jwtService.isTokenValid(tokenWithInvalidSignature));
        }

    }
}