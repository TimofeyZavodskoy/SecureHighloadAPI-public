package ru.hotdog.SecureHighloadAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hotdog.SecureHighloadAPI.entities.RefreshToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRep extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti);
    void deleteByUsername(String token);
    List<RefreshToken> findAllByUsernameAndRevokedFalse(String username);
    Optional<RefreshToken> findByToken(String token); // Новый метод

}
