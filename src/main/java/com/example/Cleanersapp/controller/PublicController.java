package com.example.Cleanersapp.controller;

import com.example.Cleanersapp.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints that work without logging in, so visitors can browse before signing up.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/cleaners")
    public ResponseEntity<?> getCleaners() {
        return ResponseEntity.ok(profileService.getPublicCleaners());
    }
}
