package ua.azaika.serverpulse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.azaika.serverpulse.dto.auth.JwtAuthenticationResponseDTO;
import ua.azaika.serverpulse.dto.auth.SignInRequestDTO;
import ua.azaika.serverpulse.dto.auth.SignUpRequestDTO;
import ua.azaika.serverpulse.security.CustomUserDetails;
import ua.azaika.serverpulse.entity.Role;
import ua.azaika.serverpulse.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Andrii Zaika
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponseDTO signUp(SignUpRequestDTO request) {
        log.info("Register new user: {}", request.username());
        UserEntity userEntity = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(List.of(Role.USER))
                .createdAt(LocalDateTime.now())
                .build();

        userService.create(userEntity);

        log.info("Registration successful for user: {}", request.username());

        return generateTokenByUser(userEntity);
    }

    public JwtAuthenticationResponseDTO signIn(SignInRequestDTO request) {
        log.info("Sign in for user: {}", request.login());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                ));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Successful login attempt for user: {}", request.login());

        return generateTokenByUser(userDetails.getUser());
    }

    private JwtAuthenticationResponseDTO generateTokenByUser(UserEntity userEntity) {
        CustomUserDetails userDetails = new CustomUserDetails(userEntity);

        String jwt = jwtService.generateToken(userDetails);

        return new JwtAuthenticationResponseDTO(jwt);
    }
}