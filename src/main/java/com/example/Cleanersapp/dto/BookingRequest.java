package com.example.Cleanersapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private Long matchId;
    private LocalDateTime startAt;
    private Double durationHours;
    private String address;
    private String notes;
}
