package ru.hotdog.SecureHighloadAPI.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hotdog.SecureHighloadAPI.entities.RefreshToken;
import ru.hotdog.SecureHighloadAPI.repositories.RefreshTokenRep;

import java.time.Instant;

@Service
@AllArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRep refreshTokenRep;

    @Transactional
    public RefreshToken save(String username,String token, String jti, Instant expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setToken(token);
        refreshToken.setJti(jti);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(false);
        return refreshTokenRep.save(refreshToken);
    }

    public boolean isValid(String jti, Instant tokenExpFromJwt) {
        return refreshTokenRep.findByJti(jti)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    @Transactional
    public void revoke(String jti) {
        refreshTokenRep.findByJti(jti).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRep.save(rt);
        });
    }

    @Transactional
    public void revokeAllByUsername(String username) {
        refreshTokenRep.findAllByUsernameAndRevokedFalse(username)
                .forEach(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRep.save(rt);
                });
    }
}
