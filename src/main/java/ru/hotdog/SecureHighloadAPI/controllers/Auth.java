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
import ru.hotdog.SecureHighloadAPI.mappers.UserMapper;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;
import ru.hotdog.SecureHighloadAPI.security.JwtConfig;
import ru.hotdog.SecureHighloadAPI.services.UserDetailsImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
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
    public ResponseEntity<?> signup(@Valid @RequestBody Signup signupRequest) {
        if (userRep.existsUserByUsername(signupRequest.getUsername())) {
            System.out.println("user already exists");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Username already exists, choose another one");
        }
        if (userRep.existsUserByEmail(signupRequest.getEmail())) {
            System.out.println("email already exists");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email already exists, choose another one");
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
                .body(userResponse);
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
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Bad credentials");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtConfig.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRep.findById(userDetails.getId()).orElse(null);
        UserResponse userResponse = userMapper.toUserResponse(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userResponse);

        return ResponseEntity.ok(response);
     }
}
