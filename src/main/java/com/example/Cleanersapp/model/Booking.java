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
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "homeowner_id", nullable = false)
    private Long homeownerId;

    @Column(name = "cleaner_id", nullable = false)
    private Long cleanerId;

    // Whoever proposed the slot; the other party confirms or declines
    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "duration_hours", nullable = false)
    private Double durationHours;

    // Snapshot of the cleaner's rate when the booking was made
    @Column(name = "hourly_rate", nullable = false)
    private Integer hourlyRate;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancel_reason")
    private String cancelReason;

    private Integer rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getEndAt() {
        return startAt.plusMinutes(Math.round(durationHours * 60));
    }

    public double getTotalAmount() {
        return hourlyRate * durationHours;
    }

    public enum Status {
        REQUESTED, CONFIRMED, DECLINED, CANCELLED, COMPLETED
    }
}
