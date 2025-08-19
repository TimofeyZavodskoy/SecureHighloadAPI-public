package ru.hotdog.SecureHighloadAPI.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.hotdog.SecureHighloadAPI.dtos.AdminDto;
import ru.hotdog.SecureHighloadAPI.dtos.UserResponse;
import ru.hotdog.SecureHighloadAPI.entities.User;
import ru.hotdog.SecureHighloadAPI.exceptions.GExceptionsHandler;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;
import ru.hotdog.SecureHighloadAPI.services.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class Admin {
    private final UserService userService;

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GExceptionsHandler.ApiResponse<UserResponse>> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/get/{id}")
    public ResponseEntity<GExceptionsHandler.ApiResponse<AdminDto>> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);

        return ResponseEntity.ok(
                GExceptionsHandler.ApiResponse.<AdminDto>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .message("Getted user with id {}")
                        .data(AdminDto.fromUser(user))
                        .build());
    }
}
