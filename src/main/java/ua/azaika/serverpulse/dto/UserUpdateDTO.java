package ua.azaika.serverpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;
import ua.azaika.serverpulse.entity.Role;

import java.util.List;

/**
 @author Andrii Zaika
 **/
public record UserUpdateDTO(
        @NotBlank
        @Length(min = 3, max = 64)
        String username,
        @NotBlank
        @Length(min = 3, max = 64)
        String password,
        @NotBlank
        @Length(min = 3, max = 64)
        @Email
        String email,
        @NotEmpty
        List<Role> roles
) {
}
