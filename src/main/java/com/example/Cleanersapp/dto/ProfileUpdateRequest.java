package com.example.Cleanersapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateRequest {
    private String name;
    private String bio;
    private Integer hourlyRate;      // only for cleaners
    private String profilePicture;   // URL (we'll use text URL for now, not upload)
    private String headline;
    private List<String> services;
    private List<String> languages;
    private List<String> availableDays;
    private String serviceArea;
    private Integer yearsExperience;
    private Boolean bringsSupplies;
    private Boolean petFriendly;
    private Boolean acceptsDirectRequests;
}
