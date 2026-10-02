package com.example.Cleanersapp.service;

import com.example.Cleanersapp.dto.ProfileResponse;
import com.example.Cleanersapp.dto.ProfileUpdateRequest;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    public ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    public ProfileResponse getProfile(Long userId) {
        return getMyProfile(userId);
    }

    public ProfileResponse updateMyProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        // Hourly rate can only be updated by cleaners
        if (request.getHourlyRate() != null) {
            if (user.getRole() != User.Role.CLEANER) {
                throw new RuntimeException("Only cleaners can set hourly rate");
            }
            if (request.getHourlyRate() < 0) {
                throw new RuntimeException("Hourly rate must be positive");
            }
            user.setHourlyRate(request.getHourlyRate());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getBio(),
                user.getHourlyRate(),
                user.getRating(),
                user.getProfilePicture()
        );
    }
}