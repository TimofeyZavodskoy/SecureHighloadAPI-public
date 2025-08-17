package ru.hotdog.SecureHighloadAPI.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.hotdog.SecureHighloadAPI.dtos.Signup;
import ru.hotdog.SecureHighloadAPI.entities.Role;
import ru.hotdog.SecureHighloadAPI.entities.User;
import ru.hotdog.SecureHighloadAPI.exceptions.AppException;
import ru.hotdog.SecureHighloadAPI.repositories.RoleRep;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRep userRep;
    private final PasswordEncoder passwordEncoder;
    private final RoleRep roleRep;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRep.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User '%s' not found", username)
                ));
        return UserDetailsImpl.build(user);
    }

    public User createUser(Signup signup) {
        if (userRep.existsUserByUsername(signup.getUsername())) {
            log.error("Username already exists");
            throw new AppException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRep.existsUserByEmail(signup.getEmail())) {
            log.error("Email already exists");
            throw new AppException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setUsername(signup.getUsername());
        user.setPassword(passwordEncoder.encode(signup.getPassword()));
        user.setEmail(signup.getEmail());

        Role role = roleRep.findByName("USER")
                .orElseThrow(()-> new RuntimeException("Role ROLE_USER not found"));

        user.setRoles(List.of(role));
        System.out.println(role);

        return userRep.save(user);
    }

    public void assignRoleToUser(Long id, String name) {
        User user = userRep.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found '{}'", id);
                    return new RuntimeException("User not found");
                });

        Role role = roleRep.findByName(name)
                .orElseThrow(()-> {
                    log.error("Role not found '{}'", name);
                    return new RuntimeException("Role not found");
                });

        user.getRoles().add(role);
        userRep.save(user);
        log.info("Role '{}' assigned to user '{}'", name, id);
    }
}
