package com.example.Cleanersapp.controller;

import com.example.Cleanersapp.dto.ProfileResponse;
import com.example.Cleanersapp.dto.ProfileUpdateRequest;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.UserRepository;
import com.example.Cleanersapp.service.ProfileService;
import com.example.Cleanersapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId(String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    // Get my own profile
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getCurrentUserId(authHeader);
            return ResponseEntity.ok(profileService.getMyProfile(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Update my own profile
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ProfileUpdateRequest request) {
        try {
            Long userId = getCurrentUserId(authHeader);
            ProfileResponse updated = profileService.updateMyProfile(userId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // View someone else's profile
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(profileService.getProfile(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
