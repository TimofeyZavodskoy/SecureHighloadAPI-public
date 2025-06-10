package ru.hotdog.SecureHighloadAPI.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roleName;

    @OneToMany
    @JoinColumn(name = "role_id")
    private List<User> users;
}
