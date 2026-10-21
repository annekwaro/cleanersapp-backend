package com.example.Cleanersapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long matchId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfilePicture;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Double durationHours;
    private Integer hourlyRate;
    private Double totalAmount;
    private String address;
    private String notes;
    private String status;
    private String cancelReason;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Viewer-specific flags so the client doesn't re-implement the rules
    private boolean requestedByMe;
    private boolean cancelledByMe;
    private boolean canRespond;
    private boolean canCancel;
    private boolean canComplete;
    private boolean canRate;
}
