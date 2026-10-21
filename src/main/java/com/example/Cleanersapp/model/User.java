package com.example.Cleanersapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    private String bio;

    @Column(name = "hourly_rate")
    private Integer hourlyRate;

    private Double rating;

    @Column(name = "profile_picture")
    private String profilePicture;

    // One-line pitch shown on the swipe card, e.g. "Deep-clean specialist, 6 yrs"
    private String headline;

    // Comma-separated lists, e.g. "Deep clean,Laundry" / "MON,TUE,SAT"
    private String services;

    private String languages;

    @Column(name = "available_days")
    private String availableDays;

    @Column(name = "service_area")
    private String serviceArea;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "brings_supplies")
    private Boolean bringsSupplies;

    @Column(name = "pet_friendly")
    private Boolean petFriendly;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Role {
        HOMEOWNER, CLEANER
    }
}