package ru.hotdog.SecureHighloadAPI.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.hotdog.SecureHighloadAPI.dtos.Signin;
import ru.hotdog.SecureHighloadAPI.dtos.Signup;
import ru.hotdog.SecureHighloadAPI.dtos.UserResponse;
import ru.hotdog.SecureHighloadAPI.entities.User;
import ru.hotdog.SecureHighloadAPI.exceptions.AppException;
import ru.hotdog.SecureHighloadAPI.exceptions.GExceptionsHandler;
import ru.hotdog.SecureHighloadAPI.mappers.UserMapper;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;
import ru.hotdog.SecureHighloadAPI.security.JwtConfig;
import ru.hotdog.SecureHighloadAPI.services.UserDetailsImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class Auth {
    private final UserRep userRep;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;

    @PostMapping("/signup/save")
    public ResponseEntity<GExceptionsHandler.ApiResponse<UserResponse>> signup(@Valid @RequestBody Signup signupRequest) {
        if (userRep.existsUserByUsername(signupRequest.getUsername())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRep.existsUserByEmail(signupRequest.getEmail())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        log.info("signup request received");
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        userRep.save(user);

        UserResponse userResponse = userMapper.toUserResponse(user);
        log.info("save user response received");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GExceptionsHandler.ApiResponse.<UserResponse>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.CREATED.value())
                        .message("User registered successfully")
                        .data(userResponse)
                        .build());
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody Signin signinRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signinRequest.getUsername(),
                            signinRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtConfig.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRep.findById(userDetails.getId()).orElse(null);
        UserResponse userResponse = userMapper.toUserResponse(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userResponse);

        return ResponseEntity.ok(
                GExceptionsHandler.ApiResponse.<Map<String, Object>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .message("Signin successful")
                        .data(response)
                        .build());
     }
}
