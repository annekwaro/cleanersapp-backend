package com.example.Cleanersapp.controller;

import com.example.Cleanersapp.dto.MatchResponse;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.UserRepository;
import com.example.Cleanersapp.service.SwipeService;
import com.example.Cleanersapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SwipeController {

    @Autowired
    private SwipeService swipeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    /**
     * Extracts the current user's ID from the JWT token.
     */
    private Long getCurrentUserId(String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    // ============ SWIPE ============
    @PostMapping("/swipe/{targetId}")
    public ResponseEntity<?> swipe(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long targetId,
            @RequestParam String direction) {

        try {
            Long userId = getCurrentUserId(authHeader);
            Map<String, Object> result = swipeService.swipe(userId, targetId, direction);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============ GET SWIPEABLE PROFILES ============
    @GetMapping("/swipe/profiles")
    public ResponseEntity<?> getSwipeableProfiles(@RequestHeader("Authorization") String authHeader) {
        Long userId = getCurrentUserId(authHeader);
        List<User> profiles = swipeService.getSwipeableProfiles(userId);

        // Return only safe fields
        List<Map<String, Object>> safeProfiles = profiles.stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "role", u.getRole().name(),
                        "bio", u.getBio() == null ? "" : u.getBio(),
                        "hourlyRate", u.getHourlyRate() == null ? 0 : u.getHourlyRate(),
                        "rating", u.getRating() == null ? 0 : u.getRating(),
                        "profilePicture", u.getProfilePicture() == null ? "" : u.getProfilePicture()
                ))
                .toList();

        return ResponseEntity.ok(safeProfiles);
    }

    // ============ GET MY MATCHES ============
    @GetMapping("/matches")
    public ResponseEntity<?> getMatches(@RequestHeader("Authorization") String authHeader) {
        Long userId = getCurrentUserId(authHeader);
        List<MatchResponse> matches = swipeService.getMatches(userId);
        return ResponseEntity.ok(matches);
    }
}