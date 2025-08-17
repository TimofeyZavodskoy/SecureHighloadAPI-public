package ru.hotdog.SecureHighloadAPI.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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
import org.springframework.web.bind.annotation.*;
import ru.hotdog.SecureHighloadAPI.dtos.Signin;
import ru.hotdog.SecureHighloadAPI.dtos.Signup;
import ru.hotdog.SecureHighloadAPI.dtos.UserResponse;
import ru.hotdog.SecureHighloadAPI.entities.RefreshToken;
import ru.hotdog.SecureHighloadAPI.entities.User;
import ru.hotdog.SecureHighloadAPI.exceptions.AppException;
import ru.hotdog.SecureHighloadAPI.exceptions.GExceptionsHandler;
import ru.hotdog.SecureHighloadAPI.mappers.UserMapper;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;
import ru.hotdog.SecureHighloadAPI.security.configs.JwtConfig;
import ru.hotdog.SecureHighloadAPI.services.RefreshTokenService;
import ru.hotdog.SecureHighloadAPI.services.UserDetailsImpl;
import ru.hotdog.SecureHighloadAPI.services.UserService;

import java.time.Instant;
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
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @PostMapping("/signup/save")
    public ResponseEntity<GExceptionsHandler.ApiResponse<UserResponse>> signup(@Valid @RequestBody Signup signupRequest) {
//        if (userRep.existsUserByUsername(signupRequest.getUsername())) {
//            throw new AppException(HttpStatus.BAD_REQUEST, "Username already exists");
//        }
//        if (userRep.existsUserByEmail(signupRequest.getEmail())) {
//            throw new AppException(HttpStatus.BAD_REQUEST, "Email already exists");
//        }
//
//        log.info("signup request received");
//        User user = new User();
//        user.setUsername(signupRequest.getUsername());
//        user.setEmail(signupRequest.getEmail());
//        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
//        userRep.save(user);
        User user = userService.createUser(signupRequest);
        UserResponse userResponse = userMapper.toUserResponse(user);
        log.info("save user response received");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GExceptionsHandler.ApiResponse.<UserResponse>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.CREATED.value())
                        .message("User registered successfully with ROLE_USER")
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
        String access = jwtConfig.generateAccessToken(authentication);
        var refreshPair = jwtConfig.generateRefreshToken(authentication);

        String username = signinRequest.getUsername();

        Jws<Claims> parsed = jwtConfig.parseToken(refreshPair.token());
        refreshTokenService.save(
                username,
                jwtConfig.getJti(parsed),
                jwtConfig.getExpiration(parsed)
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRep.findById(userDetails.getId()).orElse(null);
        UserResponse userResponse = userMapper.toUserResponse(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token pair", new TokenResponse(access, refreshPair.token()));
        response.put("user", userResponse);


        return ResponseEntity.ok(
                GExceptionsHandler.ApiResponse.<Map<String, Object>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .message("Signin successful")
                        .data(response)
                        .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshToken refreshToken) {
        try {

            Jws<Claims> jws = jwtConfig.parseToken(String.valueOf(refreshToken));
            if (!jwtConfig.isRefresh(jws)) {
                throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid");
            }

            String username = jwtConfig.getUsernameFromToken(String.valueOf(jws));
            String oldJti = jwtConfig.getJti(jws);
            Instant expiration = jwtConfig.getExpiration(jws);

            if (!refreshTokenService.isValid(oldJti, expiration)) {
                throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid/revoked");
            }

            refreshTokenService.revoke(oldJti);

            String newAccess = jwtConfig.generateAccessToken(username);
            var newRefreshPair = jwtConfig.generateRefreshToken(username);

            Jws<Claims> newParsed = jwtConfig.parseToken(newRefreshPair.token());
            refreshTokenService.save(username, jwtConfig.getJti(newParsed), jwtConfig.getExpiration(newParsed));

            Map<String, Object> response = new HashMap<>();
            response.put("token pair", new TokenResponse(newAccess, newRefreshPair.token()));

            return ResponseEntity.ok(
                    GExceptionsHandler.ApiResponse.<Map<String, Object>>builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.OK.value())
                            .message("Refresh token successful")
                            .data(response)
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid/revoked");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String username) {
        refreshTokenService.revokeAllByUsername(username);

        return ResponseEntity.ok(
                GExceptionsHandler.ApiResponse.<Map<String, Object>>builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .message("Logged out")
                        .build()
        );
    }


    public record TokenResponse(String accessToken, String refreshToken) {}

}
