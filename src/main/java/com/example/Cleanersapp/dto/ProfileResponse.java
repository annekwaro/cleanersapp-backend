package com.example.Cleanersapp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private Long id;
    private String email;           // only filled for the owner's own profile
    private String name;
    private String role;
    private String bio;
    private Integer hourlyRate;
    private Double rating;
    private String profilePicture;
    private String headline;
    private List<String> services;
    private List<String> languages;
    private List<String> availableDays;
    private String serviceArea;
    private Integer yearsExperience;
    private Boolean bringsSupplies;
    private Boolean petFriendly;
    private boolean acceptsDirectRequests;
    private long completedJobs;
}
