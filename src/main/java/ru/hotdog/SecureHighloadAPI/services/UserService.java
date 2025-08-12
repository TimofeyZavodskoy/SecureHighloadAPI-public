package ru.hotdog.SecureHighloadAPI.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.hotdog.SecureHighloadAPI.entities.User;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRep userRep;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRep.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User '%s' not found", username)
                ));
        return UserDetailsImpl.build(user);
    }
}
