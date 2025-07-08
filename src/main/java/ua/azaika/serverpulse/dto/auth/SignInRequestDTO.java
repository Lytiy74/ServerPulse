package ua.azaika.serverpulse.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Sign in request")
public record SignInRequestDTO(
        @Schema(description = "User login", example = "vladimir")
        @Size(min = 5, max = 50, message = "Login length must be min = 5 and max = 50 characters")
        @NotBlank(message = "Username cannot be blank")
        String login,
        @Schema(description = "Password", example = "stringstring")
        @Size(min = 8, max = 255, message = "Password length must be from 8 to 255 characters")
        @NotBlank(message = "Password cannot be blank")
        String password
) {
}
