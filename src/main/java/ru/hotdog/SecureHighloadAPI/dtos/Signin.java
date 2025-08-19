package ru.hotdog.SecureHighloadAPI.dtos;

import lombok.Data;
import lombok.ToString;
import jakarta.validation.constraints.NotEmpty;

@Data
@ToString(exclude = "password")
public class Signin {
    @NotEmpty(message = "username couldn't be empty")
    private String username;
    @NotEmpty(message = "password couldn't be empty")
    private String password;
}
