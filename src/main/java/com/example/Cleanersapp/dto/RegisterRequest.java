package com.example.Cleanersapp.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String role;
    private String bio;
    private Integer hourlyRate;
}