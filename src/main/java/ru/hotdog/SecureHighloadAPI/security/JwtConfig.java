package ru.hotdog.SecureHighloadAPI.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.hotdog.SecureHighloadAPI.services.UserDetailsImpl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtConfig {
    @Value("${api.app.secret}")
    private String secret;
    @Value("${api.app.lifetime}")
    private int lifetime;

    public String generateToken(Authentication authentication) {
        log.info("Generating token");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + lifetime))
                .signWith(getSigninKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        log.info("Getting username from token");
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build();
        return parser.parseClaimsJws(token).getBody().getSubject();
    }

    private Key getSigninKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
