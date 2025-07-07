package ua.azaika.serverpulse.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


@Service
@Slf4j
public class JwtService {

    // jwt - Это header.payload.signature
    // -> HEADER - алгоритм + имя токена
    // -> payload - это claims (имя кота, наше имя, роль, дата создания + окончания токена)
    // -> эта часть гарантирует целостность токена, типа не изменят payload потому что подпись(этот блок) не сойдется

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.token.expirationInMillis}")
    private long expirationTime; // 24 часа

    private SecretKey signingKey;

    /**
     * Ключ декодируется и создаётся один раз при старте — оптимизация.
     */
    @PostConstruct
    private void initSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.debug("JWT signing key initialized.");
    }


    /**
     * Генерация простого токена (без дополнительных claims).
     */
    public String generateToken(UserDetails userDetails) {
        return generateTokenWithClaims(userDetails, Map.of());
    }

    /**
     * Генерация токена с дополнительными claims.
     */
    public String generateTokenWithClaims(UserDetails userDetails, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationTime);

        return Jwts.builder().claims(extraClaims) // доп. поля мжт мы захочем в него положить имя своего кота, хз
                .subject(userDetails.getUsername()) // для кого токен
                .issuedAt(now) // когда подписали?
                .expiration(expiry) // когда п*зда токену?)
                .signWith(signingKey) // вот тут важно, факт того что мы (сервер) подписали токен,
                // а выше метод initSigningKey() где мы вставили секрет и сгенерировали SecretKey
                .compact();
    }

    /**
     * Извлечение всех claims, обернутое в Optional — безопаснее.
     */
    public Optional<Claims> tryExtractAllClaims(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload());
        } catch (JwtException e) {
            log.warn("JWT parsing failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Универсальный метод для извлечения конкретного claim.
     */
    public <T> Optional<T> extractClaim(String token, Function<Claims, T> resolver) {
        return tryExtractAllClaims(token).map(resolver);
    }

    /**
     * Извлечение логина (subject) из токена.
     */
    public Optional<String> extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Проверка, не протух ли токен.
     */
    public boolean isTokenNotExpired(String token) {
        return extractClaim(token, Claims::getExpiration).map(exp -> exp.after(new Date())).orElse(false);
    }

    /**
     * Валидность токена по сроку + что логин совпадает.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).map(username -> username.equals(userDetails.getUsername()) && isTokenNotExpired(token)).orElse(false);
    }

}
