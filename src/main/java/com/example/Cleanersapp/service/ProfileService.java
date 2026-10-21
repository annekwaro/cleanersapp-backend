package com.example.Cleanersapp.service;

import com.example.Cleanersapp.dto.ProfileResponse;
import com.example.Cleanersapp.dto.ProfileUpdateRequest;
import com.example.Cleanersapp.model.Booking;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.BookingRepository;
import com.example.Cleanersapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class ProfileService {

    // users.bio was created as varchar(255) and ddl-auto=update won't widen it
    private static final int MAX_BIO = 255;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ProfileResponse response = toCard(user);
        response.setEmail(user.getEmail());
        return response;
    }

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toCard(user);
    }

    /**
     * Cleaners shown to visitors who haven't signed up yet, best rated first.
     */
    public List<ProfileResponse> getPublicCleaners() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.CLEANER)
                .sorted(Comparator.comparing((User u) -> u.getRating() == null ? 0 : u.getRating()).reversed())
                .limit(50)
                .map(this::toCard)
                .toList();
    }

    public ProfileResponse updateMyProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getBio() != null) {
            if (request.getBio().length() > MAX_BIO) {
                throw new RuntimeException("Bio must be " + MAX_BIO + " characters or fewer");
            }
            user.setBio(request.getBio());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
        if (request.getHeadline() != null) {
            user.setHeadline(request.getHeadline().trim());
        }
        if (request.getServices() != null) {
            user.setServices(join(request.getServices()));
        }
        if (request.getLanguages() != null) {
            user.setLanguages(join(request.getLanguages()));
        }
        if (request.getAvailableDays() != null) {
            user.setAvailableDays(join(request.getAvailableDays()));
        }
        if (request.getServiceArea() != null) {
            user.setServiceArea(request.getServiceArea().trim());
        }
        if (request.getYearsExperience() != null) {
            user.setYearsExperience(Math.max(0, request.getYearsExperience()));
        }
        if (request.getBringsSupplies() != null) {
            user.setBringsSupplies(request.getBringsSupplies());
        }
        if (request.getPetFriendly() != null) {
            user.setPetFriendly(request.getPetFriendly());
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
        return getMyProfile(userId);
    }

    /**
     * Profile without private fields, safe to show to other users or visitors.
     */
    public ProfileResponse toCard(User user) {
        long completedJobs = user.getRole() == User.Role.CLEANER
                ? bookingRepository.findByCleanerIdAndStatus(user.getId(), Booking.Status.COMPLETED).size()
                : 0;

        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .bio(user.getBio())
                .hourlyRate(user.getHourlyRate())
                .rating(user.getRating())
                .profilePicture(user.getProfilePicture())
                .headline(user.getHeadline())
                .services(split(user.getServices()))
                .languages(split(user.getLanguages()))
                .availableDays(split(user.getAvailableDays()))
                .serviceArea(user.getServiceArea())
                .yearsExperience(user.getYearsExperience())
                .bringsSupplies(user.getBringsSupplies())
                .petFriendly(user.getPetFriendly())
                .completedJobs(completedJobs)
                .build();
    }

    private String join(List<String> values) {
        return String.join(",", values.stream().map(String::trim).filter(v -> !v.isEmpty()).toList());
    }

    private List<String> split(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(v -> !v.isEmpty()).toList();
    }
}
