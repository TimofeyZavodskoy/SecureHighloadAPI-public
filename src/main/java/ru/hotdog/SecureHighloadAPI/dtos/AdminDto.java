package ru.hotdog.SecureHighloadAPI.dtos;

import ru.hotdog.SecureHighloadAPI.entities.Role;
import ru.hotdog.SecureHighloadAPI.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public record AdminDto(
        Long id,
        String username,
        String email,
        List<String> roles
) {
    public static AdminDto fromUser(User user) {
        return new AdminDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList())
        );
    }
}