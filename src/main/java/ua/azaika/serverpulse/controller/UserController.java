package ua.azaika.serverpulse.controller;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.azaika.serverpulse.dto.auth.UserResponseDTO;
import ua.azaika.serverpulse.service.UserService;

/**
 @author Andrii Zaika
 **/
@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @GetMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> getByUsername(@PathVariable String uuid) {
        return ResponseEntity.ok(userService.findByUsername(uuid));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

}
