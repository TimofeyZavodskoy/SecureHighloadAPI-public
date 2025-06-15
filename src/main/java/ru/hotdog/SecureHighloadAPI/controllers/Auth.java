package ru.hotdog.SecureHighloadAPI.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.hotdog.SecureHighloadAPI.dtos.Signin;
import ru.hotdog.SecureHighloadAPI.dtos.Signup;
import ru.hotdog.SecureHighloadAPI.entities.User;
import ru.hotdog.SecureHighloadAPI.repositories.UserRep;
import ru.hotdog.SecureHighloadAPI.security.JwtConfig;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class Auth {
    private final UserRep userRep;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;

    @GetMapping("/signin")
    public String showSigninForm() {
        System.out.println("SigninForm");
        System.out.println("Username: " + SecurityContextHolder.getContext().getAuthentication().getName());
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        Signup user = new Signup();
        model.addAttribute("user", user);
        System.out.println("SignupForm");
        return "registraton";
    }

    @PostMapping("/signup/save")
    public ResponseEntity<?> signup(@Valid @RequestBody Signup signupRequest) {
        if (userRep.existsUserByUsername(signupRequest.getUsername())) {
            System.out.println("user already exists");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Username already exists, choose another one");
        }
        if (userRep.existsUserByEmail(signupRequest.getEmail())) {
            System.out.println("email already exists");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Email already exists, choose another one");
        }
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        userRep.save(user);
        System.out.println("saved user");
        return ResponseEntity.ok("Signup successful");
    }
//    @PostMapping("/signup/save")
//    public String signup(@Valid @ModelAttribute("user") Signup user, BindingResult result, Model model) {
//        User existingUser = userRep.findByEmail(user.getEmail());
//        System.out.println(user.getEmail());
//        if(existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()){
//            result.rejectValue("email", null,
//                    "There is already an account registered with the same email");
//        }
//        userService.saveUser(user);
//        return "redirect:/register?success";
//    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody Signin signinRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signinRequest.getUsername(),
                            signinRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Bad credentials");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtConfig.generateToken(authentication);
        return ResponseEntity.ok(jwt);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        });
        return ResponseEntity.badRequest().body(errors.toString());
    }
}
