package ua.azaika.serverpulse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;
import ua.azaika.serverpulse.entity.Role;

import java.util.List;

/**
 @author Andrii Zaika
 **/
@Schema(description = "DTO for patching user information")
public record UserPatchDTO(
        @Length(min = 3, max = 64)
        @Schema(description = "User's username", example = "ServerPulse")
        String username,
        @Length(min = 3, max = 64)
        @Schema(description = "User's password", example = "<PASSWORD>")
        String password,
        @Email
        @Schema(description = "User's email address", example = "server@pulse.com")
        String email,
        @Schema(description = "List of user roles", example = "[\"USER\", \"ADMIN\"]")
        List<Role> roles
) {
}
