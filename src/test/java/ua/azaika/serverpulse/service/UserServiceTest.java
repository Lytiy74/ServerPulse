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

package ua.azaika.serverpulse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.azaika.serverpulse.dto.UserPatchDTO;
import ua.azaika.serverpulse.dto.UserUpdateDTO;
import ua.azaika.serverpulse.dto.auth.UserResponseDTO;
import ua.azaika.serverpulse.entity.Role;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.exception.UserAlreadyExistsException;
import ua.azaika.serverpulse.exception.UserNotFoundException;
import ua.azaika.serverpulse.mapper.UserMapper;
import ua.azaika.serverpulse.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrii Zaika
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserEntity userEntity;

    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        this.userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("user")
                .email("testmail@serverpulse")
                .password("supersecurepassword")
                .createdAt(LocalDateTime.now())
                .roles(List.of(Role.USER))
                .build();

        this.userResponseDTO = new UserResponseDTO("user", "testmail@serverpulse", List.of(Role.USER));
    }

    @Test
    void create_noUserExists_shouldCreateUser() {
        when(userRepository.existsByUsername(userEntity.getUsername())).thenReturn(false);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.create(userEntity);

        assertEquals(userResponseDTO, response);
    }


    @Test
    void create_userExists_shouldThrowException() {
        when(userRepository.existsByUsername(userEntity.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.create(userEntity));
    }

    @Test
    void findById_userExists_shouldReturnUser() {
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO byId = userService.findById(userEntity.getId().toString());

        assertEquals(userResponseDTO, byId);
    }

    @Test
    void findById_userNotExists_shouldThrowException() {
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.empty());

        String uuid = userEntity.getId().toString();
        assertThrows(UserNotFoundException.class, () -> userService.findById(uuid));
    }

    @Test
    void getAll_shouldReturnUsers() {
        Pageable pageable = Pageable.ofSize(10);
        List<UserEntity> userEntityList = List.of(userEntity);
        Page<UserEntity> userEntityPage = new PageImpl<>(userEntityList, pageable, 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userEntityPage);
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDTO);

        Page<UserResponseDTO> resultPage = userService.getAll(pageable);

        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());
        assertEquals(userResponseDTO, resultPage.getContent().get(0));

        verify(userRepository).findAll(pageable);
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void patch_userExists_shouldUpdatePartialFields() {
        UserPatchDTO patchDTO = new UserPatchDTO("newUsername", null, "newEmail@serverpulse.com", null);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.patch(userEntity.getId().toString(), patchDTO);

        assertEquals(userResponseDTO, response);
        verify(userRepository).save(userEntity);
        assertEquals("newUsername", userEntity.getUsername());
        assertEquals("newEmail@serverpulse.com", userEntity.getEmail());
    }

    @Test
    void patch_userNotExists_shouldThrowException() {
        UserPatchDTO patchDTO = new UserPatchDTO("newUsername", null, "newEmail@serverpulse.com", null);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.patch(userEntity.getId().toString(), patchDTO));
    }


    @Test
    void update_userExists_shouldUpdateAllFields() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("updatedUsername", "updatedPassword", "updatedEmail@serverpulse.com", List.of(Role.ADMIN));
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode(updateDTO.password())).thenReturn("encodedUpdatedPassword");
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO response = userService.update(userEntity.getId().toString(), updateDTO);

        assertEquals(userResponseDTO, response);
        verify(userRepository).save(userEntity);
        assertEquals("updatedUsername", userEntity.getUsername());
        assertEquals("updatedEmail@serverpulse.com", userEntity.getEmail());
        assertEquals("encodedUpdatedPassword", userEntity.getPassword());
        assertEquals(List.of(Role.ADMIN), userEntity.getRoles());
    }

    @Test
    void update_userNotExists_shouldThrowException() {
        UserUpdateDTO updateDTO = new UserUpdateDTO("updatedUsername", "updatedPassword", "updatedEmail@serverpulse.com", List.of(Role.ADMIN));
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.update(userEntity.getId().toString(), updateDTO));
    }

    @Test
    void delete_userExists_shouldDeleteUser() {
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));

        userService.delete(userEntity.getId().toString());

        verify(userRepository).delete(userEntity);
    }

    @Test
    void delete_userNotExists_shouldThrowException() {
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.empty());

        String uuid = userEntity.getId().toString();
        assertThrows(UserNotFoundException.class, () -> userService.delete(uuid));
    }
}
