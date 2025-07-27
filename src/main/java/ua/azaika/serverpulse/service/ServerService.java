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
import org.springframework.stereotype.Service;
import ua.azaika.serverpulse.dto.ServerPostDTO;
import ua.azaika.serverpulse.dto.ServerResponseDTO;
import ua.azaika.serverpulse.entity.ServerEntity;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.exception.ServerNotFoundException;
import ua.azaika.serverpulse.mapper.ServerMapper;
import ua.azaika.serverpulse.repository.ServerRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Andrii Zaika
 */
@Service
@RequiredArgsConstructor
public class ServerService {
    private final ServerRepository repository;
    private final ServerMapper mapper;

    public ServerResponseDTO createServer(ServerPostDTO dto, UserEntity owner) {
        ServerEntity server = ServerEntity.builder()
                .name(dto.name())
                .ip(dto.ip())
                .port(dto.port())
                .version(dto.version())
                .description(dto.description())
                .createdAt(LocalDateTime.now())
                .owner(owner)
                .build();
        return mapper.toDto(repository.save(server));
    }

    public ServerResponseDTO getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ServerNotFoundException("Server not found with id: " + id));
    }
}
