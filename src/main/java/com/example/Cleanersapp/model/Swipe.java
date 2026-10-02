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
@Table(name = "swipes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"swiper_id", "swiped_id"}))
public class Swipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "swiper_id", nullable = false)
    private Long swiperId;

    @Column(name = "swiped_id", nullable = false)
    private Long swipedId;

    @Column(nullable = false)
    private String direction; // "LIKE" or "PASS"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}