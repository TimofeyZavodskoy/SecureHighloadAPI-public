package ru.hotdog.SecureHighloadAPI.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/secured")
public class Main {
    @GetMapping("/user")
    public String access(Principal principal) {
        if (principal != null) {
            return principal.getName();
        }
        else {
            return "You are not logged in";
        }
    }
}
