package ru.hotdog.SecureHighloadAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hotdog.SecureHighloadAPI.entities.Role;

import java.util.Optional;

@Repository
public interface RoleRep extends JpaRepository<Role, Long> {
    Optional <Role> findByName(String name);
}
