package ua.azaika.serverpulse.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.exception.JwtTokenGenerationException;
import ua.azaika.serverpulse.exception.JwtTokenValidationException;
import ua.azaika.serverpulse.security.CustomUserDetails;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.token.expirationInMillis}")
    private long jwtExpiration;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("roles", roles);

        if (userDetails instanceof CustomUserDetails customUserDetails) {
            UserEntity userEntity = customUserDetails.getUser();
            if (userEntity.getId() != null) {
                claims.put("uuid", userEntity.getId().toString());
            } else {
                throw new JwtTokenGenerationException("User ID is null, cannot add UUID claim to JWT.");
            }
        } else {
            throw new JwtTokenGenerationException("User details is not an instance of CustomUserDetails, cannot add UUID claim to JWT.");
        }

        return buildToken(claims, userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public Optional<String> extractUsername(String token) {
        return Optional.ofNullable(extractClaim(token, Claims::getSubject));
    }

    public Optional<List<String>> extractRoles(String token) {
        return Optional.ofNullable(extractClaim(token, claims -> claims.get("roles", List.class)));
    }

    public Optional<String> extractId(String token) {
        return Optional.ofNullable(extractClaim(token, claims -> claims.get("uuid", String.class)));
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            throw new JwtTokenValidationException("JWT token validation error " + e.getMessage());
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}