package ua.azaika.serverpulse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get user by UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content())
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable String uuid) {
        return ResponseEntity.ok(service.findById(uuid));
    }

    @Operation(summary = "Get all pageable users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of users got successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))}),
    })
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @Operation(summary = "Partially update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = @Content())
    })
    @PatchMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> patch(@PathVariable String uuid, @Valid @RequestBody UserPatchDTO dto) {
        UserResponseDTO patch = service.patch(uuid, dto);
        return ResponseEntity.ok(patch);
    }

    @Operation(summary = "Fully update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content()),
            @ApiResponse(responseCode = "400", description = "Invalid input provided",
                    content = @Content())
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> put(@PathVariable String uuid, @Valid @RequestBody UserUpdateDTO dto) {
        UserResponseDTO update = service.update(uuid, dto);
        return ResponseEntity.ok(update);
    }

    @Operation(summary = "Delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content())
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> delete(@PathVariable String uuid) {
        service.delete(uuid);
        return ResponseEntity.noContent().build();
    }

}