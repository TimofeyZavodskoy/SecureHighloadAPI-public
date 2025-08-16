package ru.hotdog.SecureHighloadAPI.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import ru.hotdog.SecureHighloadAPI.dtos.UserResponse;
import ru.hotdog.SecureHighloadAPI.exceptions.AppException;
import ru.hotdog.SecureHighloadAPI.exceptions.GExceptionsHandler;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/secured")
@AllArgsConstructor
public class Main {
    private UserRep userRep;

    @GetMapping("/user")
    public String access(Principal principal) {
        if (principal != null) {
            log.info(principal.getName());
            return principal.getName();
        }
        else {
            log.error("you are not logged in");
            return "You are not logged in";
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GExceptionsHandler.ApiResponse<UserResponse>> delete(@PathVariable Long id) {
        try {
            userRep.deleteUserById(id);
        } catch (BadCredentialsException e) {
            throw new AppException(HttpStatus.FORBIDDEN, "Bad credentials");
        }
        log.info("user deleted");

        return ResponseEntity.noContent().build();
    }
}
