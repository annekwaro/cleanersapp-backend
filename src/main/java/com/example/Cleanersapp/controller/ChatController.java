package com.example.Cleanersapp.controller;

import com.example.Cleanersapp.dto.MessageRequest;
import com.example.Cleanersapp.dto.MessageResponse;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.UserRepository;
import com.example.Cleanersapp.service.ChatService;
import com.example.Cleanersapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

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

    // ============ SEND MESSAGE ============
    @PostMapping("/{matchId}")
    public ResponseEntity<?> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long matchId,
            @RequestBody MessageRequest request) {
        try {
            Long userId = getCurrentUserId(authHeader);
            MessageResponse response = chatService.sendMessage(userId, matchId, request.getContent());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============ GET MESSAGES ============
    @GetMapping("/{matchId}")
    public ResponseEntity<?> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long matchId) {
        try {
            Long userId = getCurrentUserId(authHeader);
            List<MessageResponse> messages = chatService.getMessages(userId, matchId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}