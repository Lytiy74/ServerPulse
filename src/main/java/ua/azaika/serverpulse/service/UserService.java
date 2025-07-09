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

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.azaika.serverpulse.dto.UserPatchDTO;
import ua.azaika.serverpulse.dto.UserUpdateDTO;
import ua.azaika.serverpulse.dto.auth.UserResponseDTO;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.exception.UserAlreadyExistsException;
import ua.azaika.serverpulse.exception.UserNotFoundException;
import ua.azaika.serverpulse.mapper.UserMapper;
import ua.azaika.serverpulse.repository.UserRepository;

import java.util.UUID;

/**
 * @author Andrii Zaika
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    private static final String USER_NOT_FOUND_MESSAGE = "User with uuid %s not found";

    public UserResponseDTO create(UserEntity userEntity) {
        if (repository.existsByUsername((userEntity.getUsername()))) {
            throw new UserAlreadyExistsException("User with username " + userEntity.getUsername() + " already exists");
        }

        return mapper.toDto(repository.save(userEntity));
    }

    public UserResponseDTO findById(String uuid){
        UserEntity userEntity = repository.findById(UUID.fromString(uuid)).orElseThrow(
                () -> new UserNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, uuid))
        );
        return mapper.toDto(userEntity);
    }

    public Page<UserResponseDTO> getAll(Pageable pageable) {
        Page<UserEntity> page = repository.findAll(pageable);
        return page.map(mapper::toDto);
    }

    public UserResponseDTO patch(String uuid, UserPatchDTO dto) {
        UserEntity userEntity = repository.findById(UUID.fromString(uuid)).orElseThrow(
                () -> new UserNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, uuid))
        );
        if (dto.username() != null) userEntity.setUsername(dto.username());
        if (dto.email() != null) userEntity.setEmail(dto.email());
        if (dto.password() != null) userEntity.setPassword(passwordEncoder.encode(dto.password()));
        if (dto.roles() != null) userEntity.setRoles(dto.roles());
        repository.save(userEntity);
        return mapper.toDto(userEntity);
    }

    public UserResponseDTO update(String uuid, UserUpdateDTO dto){
        UserEntity userEntity = repository.findById(UUID.fromString(uuid)).orElseThrow(
                () -> new UserNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, uuid))
        );
        userEntity.setUsername(dto.username());
        userEntity.setEmail(dto.email());
        userEntity.setPassword(passwordEncoder.encode(dto.password()));
        userEntity.setRoles(dto.roles());
        repository.save(userEntity);
        return mapper.toDto(userEntity);
    }

    public void delete(String uuid) {
        UserEntity userEntity = repository.findById(UUID.fromString(uuid)).orElseThrow(
                () -> new UserNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, uuid))
        );
        repository.delete(userEntity);
    }

}
