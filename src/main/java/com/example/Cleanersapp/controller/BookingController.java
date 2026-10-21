package com.example.Cleanersapp.controller;

import com.example.Cleanersapp.dto.BookingRequest;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.UserRepository;
import com.example.Cleanersapp.service.BookingService;
import com.example.Cleanersapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

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

    private ResponseEntity<?> handle(Supplier<Object> action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Authorization") String authHeader,
                                    @RequestBody BookingRequest request) {
        return handle(() -> bookingService.create(getCurrentUserId(authHeader), request));
    }

    @GetMapping
    public ResponseEntity<?> listMine(@RequestHeader("Authorization") String authHeader) {
        return handle(() -> bookingService.listMine(getCurrentUserId(authHeader)));
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> listForMatch(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Long matchId) {
        return handle(() -> bookingService.listForMatch(getCurrentUserId(authHeader), matchId));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return handle(() -> bookingService.confirm(getCurrentUserId(authHeader), id));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<?> decline(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return handle(() -> bookingService.decline(getCurrentUserId(authHeader), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@RequestHeader("Authorization") String authHeader, @PathVariable Long id,
                                    @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return handle(() -> bookingService.cancel(getCurrentUserId(authHeader), id, reason));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        return handle(() -> bookingService.complete(getCurrentUserId(authHeader), id));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rate(@RequestHeader("Authorization") String authHeader, @PathVariable Long id,
                                  @RequestBody Map<String, Integer> body) {
        return handle(() -> bookingService.rate(getCurrentUserId(authHeader), id, body.get("rating")));
    }
}
