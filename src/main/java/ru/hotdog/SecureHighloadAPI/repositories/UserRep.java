package ru.hotdog.SecureHighloadAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hotdog.SecureHighloadAPI.entities.User;

import java.util.Optional;

public interface UserRep extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsUserByEmail(String email);
    boolean existsUserByUsername(String username);
}
