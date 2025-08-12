package ru.hotdog.SecureHighloadAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hotdog.SecureHighloadAPI.entities.Role;

public interface RoleRep extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
