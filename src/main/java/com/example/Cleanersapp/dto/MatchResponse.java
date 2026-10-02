package com.example.Cleanersapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MatchResponse {
    private Long matchId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private String otherUserRole;
    private String otherUserProfilePicture;
    private LocalDateTime matchedAt;
}