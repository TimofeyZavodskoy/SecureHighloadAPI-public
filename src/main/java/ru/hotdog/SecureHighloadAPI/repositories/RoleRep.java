package ru.hotdog.SecureHighloadAPI.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.hotdog.SecureHighloadAPI.entities.Role;

@Repository
public interface RoleRep extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
