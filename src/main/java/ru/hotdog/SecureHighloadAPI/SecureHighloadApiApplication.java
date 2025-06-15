package ru.hotdog.SecureHighloadAPI;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.hotdog.SecureHighloadAPI.security.JwtConfig;

@SpringBootApplication
@AllArgsConstructor
public class SecureHighloadApiApplication {
	private JwtConfig jwtConfig;

	public static void main(String[] args) {
		SpringApplication.run(SecureHighloadApiApplication.class, args);
	}

}
