package com.example.Cleanersapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String bio;
    private Integer hourlyRate;
    private Double rating;
    private String profilePicture;
}
