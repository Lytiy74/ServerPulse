package ua.azaika.serverpulse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ua.azaika.serverpulse.dto.auth.UserResponseDTO;
import ua.azaika.serverpulse.entity.UserEntity;

/**
 @author Andrii Zaika
 **/
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(source = "username", target = "username")
    UserResponseDTO toDto(UserEntity userEntity);
}
