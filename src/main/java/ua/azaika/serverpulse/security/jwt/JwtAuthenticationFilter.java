package ua.azaika.serverpulse.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ua.azaika.serverpulse.security.CustomUserDetails;
import ua.azaika.serverpulse.entity.Role;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.service.JwtService;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        String jwtToken = null;
        String userIdentifier = null;
        List<String> userRoleStrings = null;
        UUID userId = null;

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(BEARER_PREFIX.length());

        try {
            Optional<String> extractedUserIdentifier = jwtService.extractUsername(jwtToken);
            Optional<List<String>> extractedRoleStrings = jwtService.extractRoles(jwtToken);
            Optional<String> extractedUserId = jwtService.extractId(jwtToken);

            if (extractedUserIdentifier.isEmpty() || extractedRoleStrings.isEmpty() || extractedUserId.isEmpty()) {
                log.warn("JWT token is missing essential claims (subject, roles, or user ID).");
                filterChain.doFilter(request, response);
                return;
            }

            userIdentifier = extractedUserIdentifier.get();
            userRoleStrings = extractedRoleStrings.get();
            userId = UUID.fromString(extractedUserId.get());

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwtToken)) {

                    List<Role> rolesAsEnums = userRoleStrings.stream()
                            .map(roleString -> Role.valueOf(roleString.replace("ROLE_", "").toUpperCase(java.util.Locale.ROOT)))
                            .toList();

                    UserEntity userFromToken = UserEntity.builder()
                            .id(userId)
                            .username(userIdentifier)
                            .roles(rolesAsEnums)
                            .password("")
                            .email("")
                            .createdAt(LocalDateTime.now())
                            .accountNonExpired(true)
                            .accountNonLocked(true)
                            .credentialsNonExpired(true)
                            .enabled(true)
                            .build();

                    UserDetails userDetails = new CustomUserDetails(userFromToken);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("User '{}' (ID: {}) authenticated successfully via stateless JWT.", userIdentifier, userId);
                } else {
                    log.warn("JWT Token for user '{}' (ID: {}) is invalid or expired.", userIdentifier, userId);
                }
            }
        } catch (Exception ex) {
            log.error("JWT authentication error for token: {}", jwtToken, ex);
            handlerExceptionResolver.resolveException(request, response, null, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }
}