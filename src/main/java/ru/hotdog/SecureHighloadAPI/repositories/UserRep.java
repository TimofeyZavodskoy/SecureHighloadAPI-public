package ru.hotdog.SecureHighloadAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hotdog.SecureHighloadAPI.entities.User;

import java.util.Optional;

public interface UserRep extends JpaRepository<User, Long> {
    UserRep findByUsername(String username);
    UserRep findByEmail(String email);
    Optional<UserRep> findByPassword(String password);
}
