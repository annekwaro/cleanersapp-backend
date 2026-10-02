package com.example.Cleanersapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long matchId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private boolean isMine; // true if the current user sent it
}