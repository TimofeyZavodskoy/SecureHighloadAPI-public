package ru.hotdog.SecureHighloadAPI.security.configs;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.hotdog.SecureHighloadAPI.services.UserDetailsImpl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtConfig {

    @Value("${api.app.secret}")
    private String secret;

    @Value("${api.app.accesslifetime}")
    private int accessLifetime;

    @Value("${api.app.refreshlifetime}")
    private int refreshLifetime;

    public String generateAccessToken(Authentication authentication) {
        String jti = UUID.randomUUID().toString();
        log.info("Generating accessToken");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessLifetime))
                .addClaims(Map.of("type", "access"))
                .signWith(getSigninKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Перегрузка
    public String generateAccessToken(String username) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
        return generateAccessToken(auth);
    }

    public JwtPair generateRefreshToken(Authentication authentication) {
        log.info("Generating refreshToken");
        String jti = UUID.randomUUID().toString();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
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
