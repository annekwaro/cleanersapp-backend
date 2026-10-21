package com.example.Cleanersapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DirectBookingResponse {
    private Long matchId;
    private BookingResponse booking;
}
