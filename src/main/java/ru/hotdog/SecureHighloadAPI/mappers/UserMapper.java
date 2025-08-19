package ru.hotdog.SecureHighloadAPI.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.hotdog.SecureHighloadAPI.dtos.UserResponse;
import ru.hotdog.SecureHighloadAPI.entities.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponses(List<User> users);
}
