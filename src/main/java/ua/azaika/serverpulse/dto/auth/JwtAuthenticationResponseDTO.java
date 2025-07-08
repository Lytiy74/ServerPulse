package ua.azaika.serverpulse.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response with access token")
public record JwtAuthenticationResponseDTO(
        @Schema(description = "Access token", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTYyMjUwNj...")
        String token) {
}
