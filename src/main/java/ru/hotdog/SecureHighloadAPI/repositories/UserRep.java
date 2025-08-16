package ru.hotdog.SecureHighloadAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hotdog.SecureHighloadAPI.entities.User;

import java.util.Optional;

@Repository
public interface UserRep extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    void deleteUserById(Long id);
    boolean existsUserByEmail(String email);
    boolean existsUserByUsername(String username);
}
