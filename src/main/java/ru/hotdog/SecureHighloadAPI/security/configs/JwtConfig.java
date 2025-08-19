package ru.hotdog.SecureHighloadAPI.security.configs;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.hotdog.SecureHighloadAPI.exceptions.AppException;
import ru.hotdog.SecureHighloadAPI.services.UserDetailsImpl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtConfig {

    private final UserDetailsService userDetailsService; // Добавляем поле
    private final String secret;
    private final int accessLifetime;
    private final int refreshLifetime;

    @Autowired
    public JwtConfig(
            UserDetailsService userDetailsService,
            @Value("${api.app.secret}") String secret,
            @Value("${api.app.accesslifetime}") int accessLifetime,
            @Value("${api.app.refreshlifetime}") int refreshLifetime
    ) {
        this.userDetailsService = userDetailsService;
        this.secret = secret;
        this.accessLifetime = accessLifetime;
        this.refreshLifetime = refreshLifetime;
    }

    public String generateAccessToken(Authentication authentication) {
        String jti = UUID.randomUUID().toString();
        log.info("Generating accessToken");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessLifetime))
                .claim("type", "access")
                .claim("roles", roles)
                .signWith(getSigninKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Перегрузка
    public String generateAccessToken(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessLifetime))
                .claim("type", "access")
                .claim("roles", roles)
                .signWith(getSigninKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public JwtPair generateRefreshToken(Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(HttpStatus.BAD_REQUEST ,"No authentication in SecurityContext - cannot generate refresh token");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new AppException(HttpStatus.BAD_REQUEST ,"Unexpected principal type: " + (principal == null ? "null" : principal.getClass().getName()));
        }

        log.info("Generating refreshToken");
        String jti = UUID.randomUUID().toString();
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = Jwts.builder()
                .setSubject(username)
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshLifetime))
                .addClaims(Map.of("type", "refresh"))
                .signWith(getSigninKey(), SignatureAlgorithm.HS512)
                .compact();
        return new JwtPair(token, jti);
    }

    // Перегрузка
    public JwtPair generateRefreshToken(String username) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        return generateRefreshToken(auth);
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigninKey()).build().parseClaimsJws(token);
    }

    public String getUsernameFromToken(String token) {
        log.info("Getting username from token");
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build();
        return parser.parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isRefresh(Jws<Claims> jws) {
        return "refresh".equals(jws.getBody().get("type", String.class));
    }

    public Instant getExpiration(Jws<Claims> jws) {
        return jws.getBody().getExpiration().toInstant();
    }

    public String getJti(Jws<Claims> jws) {
        return jws.getBody().getId();
    }

    private Key getSigninKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record JwtPair(String token, String jti) {}
}
