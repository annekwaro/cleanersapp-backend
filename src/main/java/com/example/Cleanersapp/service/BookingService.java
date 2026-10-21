package com.example.Cleanersapp.service;

import com.example.Cleanersapp.dto.BookingRequest;
import com.example.Cleanersapp.dto.BookingResponse;
import com.example.Cleanersapp.model.Booking;
import com.example.Cleanersapp.model.Match;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.BookingRepository;
import com.example.Cleanersapp.repository.MatchRepository;
import com.example.Cleanersapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Propose a cleaning slot inside a match. Either side can propose; the other side responds.
     */
    public BookingResponse create(Long userId, BookingRequest request) {
        if (request.getMatchId() == null) {
            throw new RuntimeException("matchId is required");
        }
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));
        requireParticipant(match, userId);

        if (request.getStartAt() == null || request.getStartAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Pick a start time in the future");
        }
        Double hours = request.getDurationHours();
        if (hours == null || hours < 1 || hours > 12) {
            throw new RuntimeException("Duration must be between 1 and 12 hours");
        }

        Long otherId = match.getUser1Id().equals(userId) ? match.getUser2Id() : match.getUser1Id();
        User me = userRepository.findById(userId).orElseThrow();
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (me.getRole() == other.getRole()) {
            throw new RuntimeException("A booking needs one homeowner and one cleaner");
        }
        User cleaner = me.getRole() == User.Role.CLEANER ? me : other;
        User homeowner = me.getRole() == User.Role.HOMEOWNER ? me : other;

        Booking booking = new Booking();
        booking.setMatchId(match.getId());
        booking.setHomeownerId(homeowner.getId());
        booking.setCleanerId(cleaner.getId());
        booking.setRequestedBy(userId);
        booking.setStartAt(request.getStartAt());
        booking.setDurationHours(hours);
        booking.setHourlyRate(cleaner.getHourlyRate() == null ? 0 : cleaner.getHourlyRate());
        booking.setAddress(trimToNull(request.getAddress()));
        booking.setNotes(trimToNull(request.getNotes()));
        booking.setStatus(Booking.Status.REQUESTED);
        bookingRepository.save(booking);

        return toResponse(booking, userId);
    }

    public List<BookingResponse> listMine(Long userId) {
        return bookingRepository.findAllForUser(userId).stream()
                .map(b -> toResponse(b, userId))
                .toList();
    }

    public List<BookingResponse> listForMatch(Long userId, Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        requireParticipant(match, userId);
        return bookingRepository.findByMatchIdOrderByStartAtAsc(matchId).stream()
                .map(b -> toResponse(b, userId))
                .toList();
    }

    public BookingResponse confirm(Long userId, Long bookingId) {
        Booking b = load(bookingId, userId);
        if (!canRespond(b, userId)) {
            throw new RuntimeException("Only the other party can confirm a pending request");
        }
        boolean clash = bookingRepository.findByCleanerIdAndStatus(b.getCleanerId(), Booking.Status.CONFIRMED)
                .stream()
                .anyMatch(o -> o.getStartAt().isBefore(b.getEndAt()) && b.getStartAt().isBefore(o.getEndAt()));
        if (clash) {
            throw new RuntimeException("This overlaps another confirmed booking");
        }
        b.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(b);
        return toResponse(b, userId);
    }

    public BookingResponse decline(Long userId, Long bookingId) {
        Booking b = load(bookingId, userId);
        if (!canRespond(b, userId)) {
            throw new RuntimeException("Only the other party can decline a pending request");
        }
        b.setStatus(Booking.Status.DECLINED);
        bookingRepository.save(b);
        return toResponse(b, userId);
    }

    public BookingResponse cancel(Long userId, Long bookingId, String reason) {
        Booking b = load(bookingId, userId);
        if (!canCancel(b)) {
            throw new RuntimeException("This booking can no longer be cancelled");
        }
        b.setStatus(Booking.Status.CANCELLED);
        b.setCancelledBy(userId);
        b.setCancelReason(trimToNull(reason));
        bookingRepository.save(b);
        return toResponse(b, userId);
    }

    public BookingResponse complete(Long userId, Long bookingId) {
        Booking b = load(bookingId, userId);
        if (!canComplete(b, userId)) {
            throw new RuntimeException("Only the cleaner can complete a confirmed job once it has started");
        }
        b.setStatus(Booking.Status.COMPLETED);
        bookingRepository.save(b);
        return toResponse(b, userId);
    }

    /**
     * Homeowner rates a completed job; the cleaner's rating becomes the average of all rated jobs.
     */
    public BookingResponse rate(Long userId, Long bookingId, Integer stars) {
        Booking b = load(bookingId, userId);
        if (!canRate(b, userId)) {
            throw new RuntimeException("Only the homeowner can rate a completed job, once");
        }
        if (stars == null || stars < 1 || stars > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }
        b.setRating(stars);
        bookingRepository.save(b);

        User cleaner = userRepository.findById(b.getCleanerId()).orElseThrow();
        double avg = bookingRepository.findByCleanerIdAndStatus(cleaner.getId(), Booking.Status.COMPLETED)
                .stream()
                .map(Booking::getRating)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        cleaner.setRating(Math.round(avg * 10) / 10.0);
        userRepository.save(cleaner);

        return toResponse(b, userId);
    }

    // ---- rules ----

    private boolean canRespond(Booking b, Long userId) {
        return b.getStatus() == Booking.Status.REQUESTED && !b.getRequestedBy().equals(userId);
    }

    private boolean canCancel(Booking b) {
        return (b.getStatus() == Booking.Status.REQUESTED || b.getStatus() == Booking.Status.CONFIRMED)
                && b.getStartAt().isAfter(LocalDateTime.now());
    }

    private boolean canComplete(Booking b, Long userId) {
        return b.getStatus() == Booking.Status.CONFIRMED
                && b.getCleanerId().equals(userId)
                && b.getStartAt().isBefore(LocalDateTime.now());
    }

    private boolean canRate(Booking b, Long userId) {
        return b.getStatus() == Booking.Status.COMPLETED
                && b.getHomeownerId().equals(userId)
                && b.getRating() == null;
    }

    // ---- helpers ----

    private Booking load(Long bookingId, Long userId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!b.getHomeownerId().equals(userId) && !b.getCleanerId().equals(userId)) {
            throw new RuntimeException("You are not part of this booking");
        }
        return b;
    }

    private void requireParticipant(Match match, Long userId) {
        if (!match.getUser1Id().equals(userId) && !match.getUser2Id().equals(userId)) {
            throw new RuntimeException("You are not part of this match");
        }
    }

    private String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private BookingResponse toResponse(Booking b, Long viewerId) {
        Long otherId = b.getHomeownerId().equals(viewerId) ? b.getCleanerId() : b.getHomeownerId();
        User other = userRepository.findById(otherId).orElse(null);

        return new BookingResponse(
                b.getId(),
                b.getMatchId(),
                otherId,
                other != null ? other.getName() : "Unknown",
                other != null ? other.getProfilePicture() : null,
                b.getStartAt(),
                b.getEndAt(),
                b.getDurationHours(),
                b.getHourlyRate(),
                b.getTotalAmount(),
                b.getAddress(),
                b.getNotes(),
                b.getStatus().name(),
                b.getCancelReason(),
                b.getRating(),
                b.getCreatedAt(),
                b.getUpdatedAt(),
                b.getRequestedBy().equals(viewerId),
                viewerId.equals(b.getCancelledBy()),
                canRespond(b, viewerId),
                canCancel(b),
                canComplete(b, viewerId),
                canRate(b, viewerId)
        );
    }
}
