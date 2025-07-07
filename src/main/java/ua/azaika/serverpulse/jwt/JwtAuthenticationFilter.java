package ua.azaika.serverpulse.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ua.azaika.serverpulse.entity.CustomUserDetails;
import ua.azaika.serverpulse.entity.Role;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.service.JwtService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = authHeader.substring(BEARER_PREFIX.length());
            final String userId = jwtService.extractUsername(token).orElseThrow();

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.isTokenNotExpired(token)) {

                String role = jwtService.extractAuthority(token).orElseThrow();
                UserEntity user = UserEntity.builder()
                        .id(UUID.fromString(userId))
                        .role(Role.valueOf(role))
                        .build();

                CustomUserDetails userDetails = new CustomUserDetails(user);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Token valid. Authenticated user with ID: {}", userId);
            } else {
                log.warn("Invalid JWT token for user ID: {}", userId);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("JWT token processing error", ex);
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}