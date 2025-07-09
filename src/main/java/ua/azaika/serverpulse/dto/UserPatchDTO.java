package ua.azaika.serverpulse.dto;

import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;
import ua.azaika.serverpulse.entity.Role;

import java.util.List;

/**
 @author Andrii Zaika
 **/
public record UserPatchDTO(
        @Length(min = 3, max = 64)
        String username,
        @Length(min = 3, max = 64)
        String password,
        @Email
        String email,
        List<Role> roles
) {
}
