package com.example.Cleanersapp.controller;

import com.example.Cleanersapp.dto.AuthRequest;
import com.example.Cleanersapp.dto.AuthResponse;
import com.example.Cleanersapp.dto.RegisterRequest;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.UserRepository;
import com.example.Cleanersapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role must be HOMEOWNER or CLEANER"));
        }

        if (role == User.Role.CLEANER && request.getHourlyRate() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cleaners must provide hourlyRate"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(role);
        user.setBio(request.getBio());
        user.setHourlyRate(request.getHourlyRate());
        user.setRating(0.0);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "role", user.getRole().name()
        ));
    }
}