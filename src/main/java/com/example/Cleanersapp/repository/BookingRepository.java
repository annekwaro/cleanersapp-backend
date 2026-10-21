package com.example.Cleanersapp.repository;

import com.example.Cleanersapp.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.homeownerId = :userId OR b.cleanerId = :userId ORDER BY b.startAt ASC")
    List<Booking> findAllForUser(@Param("userId") Long userId);

    List<Booking> findByMatchIdOrderByStartAtAsc(Long matchId);

    List<Booking> findByCleanerIdAndStatus(Long cleanerId, Booking.Status status);
}
