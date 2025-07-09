package ua.azaika.serverpulse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.azaika.serverpulse.dto.UserPatchDTO;
import ua.azaika.serverpulse.dto.UserUpdateDTO;
import ua.azaika.serverpulse.dto.auth.UserResponseDTO;
import ua.azaika.serverpulse.service.UserService;

/**
 @author Andrii Zaika
 **/
@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
class UserController {

    private final UserService service;

    @GetMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable String uuid) {
        return ResponseEntity.ok(service.findById(uuid));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> patch(@PathVariable String uuid, @Valid @RequestBody UserPatchDTO dto) {
        UserResponseDTO patch = service.patch(uuid, dto);
        return ResponseEntity.ok(patch);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> put(@PathVariable String uuid, @Valid @RequestBody UserUpdateDTO dto) {
        UserResponseDTO update = service.update(uuid, dto);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> delete(@PathVariable String uuid) {
        service.delete(uuid);
        return ResponseEntity.noContent().build();
    }

}
