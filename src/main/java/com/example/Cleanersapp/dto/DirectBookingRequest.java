package com.example.Cleanersapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * A homeowner asking a cleaner for a slot straight from their profile,
 * without waiting for a mutual like.
 */
@Data
public class DirectBookingRequest {
    private Long cleanerId;
    private LocalDateTime startAt;
    private Double durationHours;
    private String address;
    private String notes;
    private String message; // optional first chat message
}
