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

package ua.azaika.serverpulse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.azaika.serverpulse.dto.auth.JwtAuthenticationResponseDTO;
import ua.azaika.serverpulse.dto.auth.SignInRequestDTO;
import ua.azaika.serverpulse.dto.auth.SignUpRequestDTO;
import ua.azaika.serverpulse.service.AuthenticationService;

/**
 * @author Andrii Zaika
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Sign Up user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtAuthenticationResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = @Content())
    })
    @PostMapping("/sign-up")
    public ResponseEntity<JwtAuthenticationResponseDTO> signUp(
            @Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        return ResponseEntity.ok(authenticationService.signUp(signUpRequestDTO));
    }

    @Operation(summary = "Sign In user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User signed in successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtAuthenticationResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = @Content()),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content()),
    })
    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationResponseDTO> signIn(
            @Valid @RequestBody SignInRequestDTO signInRequestDTO) {
        return ResponseEntity.ok(authenticationService.signIn(signInRequestDTO));
    }
}
