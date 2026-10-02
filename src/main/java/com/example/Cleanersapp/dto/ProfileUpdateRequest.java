package com.example.Cleanersapp.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String name;
    private String bio;
    private Integer hourlyRate;      // only for cleaners
    private String profilePicture;   // URL (we'll use text URL for now, not upload)
}