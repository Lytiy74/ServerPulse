/*
 * MIT License
 *
 * Copyright (c) 2025 Andrii Zaika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import ua.azaika.serverpulse.dto.auth.UserResponseDTO;
import ua.azaika.serverpulse.entity.CustomUserDetails;
import ua.azaika.serverpulse.entity.Role;
import ua.azaika.serverpulse.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userService.signUp(userEntity);

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

        CustomUserDetails userEntity = (CustomUserDetails) authentication.getPrincipal();
        log.info("Successful login attempt for user: {}", request.login());

        return generateTokenByUser(userEntity.getUser());
    }

    private JwtAuthenticationResponseDTO generateTokenByUser(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userEntity.getRole());

        String jwt = jwtService.generateTokenWithClaims(new CustomUserDetails(userEntity), claims);
        return new JwtAuthenticationResponseDTO(jwt);
    }
}
