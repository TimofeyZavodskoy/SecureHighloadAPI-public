package ru.hotdog.SecureHighloadAPI.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class Signup {
    @NotEmpty(message = "username couldn't be empty")
    @Size(max = 75, message = "too much! login length should be less than 75 characters")
    private String username;
    @NotEmpty(message = "password couldn't be empty")
    @Size(min = 16, message = "password length should be at least 16 characters")
    private String password;
    @Email(message = "email must to contain '@' symbol")
    @NotEmpty(message = "email couldn't be empty")
    private String email;
}
