package ua.azaika.serverpulse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;
import ua.azaika.serverpulse.entity.Role;

import java.util.List;

/**
 @author Andrii Zaika
 **/
@Schema(description = "DTO for updating user information")
public record UserUpdateDTO(
        @NotBlank
        @Length(min = 3, max = 64)
        @Schema(description = "User's username", example = "ServerPulse")
        String username,
        @NotBlank
        @Length(min = 3, max = 64)
        @Schema(description = "User's password", example = "<PASSWORD>")
        String password,
        @NotBlank
        @Length(min = 3, max = 64)
        @Email
        @Schema(description = "User's email address", example = "server@pulse.com")
        String email,
        @NotEmpty
        @Schema(description = "List of user roles", example = "[\"USER\", \"ADMIN\"]")
        List<Role> roles
) {
}
