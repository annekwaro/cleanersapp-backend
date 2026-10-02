package com.example.Cleanersapp.service;

import com.example.Cleanersapp.dto.MatchResponse;
import com.example.Cleanersapp.model.Match;
import com.example.Cleanersapp.model.Swipe;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.MatchRepository;
import com.example.Cleanersapp.repository.SwipeRepository;
import com.example.Cleanersapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SwipeService {

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Record a swipe and return whether it created a match.
     */
    public Map<String, Object> swipe(Long swiperId, Long swipedId, String direction) {
        // Prevent swiping on yourself
        if (swiperId.equals(swipedId)) {
            throw new RuntimeException("Cannot swipe on yourself");
        }

        // Prevent duplicate swipes
        if (swipeRepository.existsBySwiperIdAndSwipedId(swiperId, swipedId)) {
            throw new RuntimeException("You already swiped on this user");
        }

        // Save the swipe
        Swipe swipe = new Swipe();
        swipe.setSwiperId(swiperId);
        swipe.setSwipedId(swipedId);
        swipe.setDirection(direction.toUpperCase());
        swipeRepository.save(swipe);

        // Only LIKEs can create matches
        if (!"LIKE".equalsIgnoreCase(direction)) {
            return Map.of("matched", false, "message", "Swipe recorded");
        }

        // Check if the other user already liked us
        Optional<Swipe> reciprocalSwipe =
                swipeRepository.findBySwiperIdAndSwipedId(swipedId, swiperId);

        boolean mutualLike = reciprocalSwipe.isPresent()
                && "LIKE".equalsIgnoreCase(reciprocalSwipe.get().getDirection());

        if (mutualLike && !matchRepository.existsBetweenUsers(swiperId, swipedId)) {
            Match match = new Match();
            match.setUser1Id(swiperId);
            match.setUser2Id(swipedId);
            matchRepository.save(match);

            return Map.of(
                    "matched", true,
                    "matchId", match.getId(),
                    "message", "It's a match!"
            );
        }

        return Map.of("matched", false, "message", "Swipe recorded");
    }

    /**
     * Return profiles the current user hasn't swiped on yet.
     * Homeowners see cleaners; cleaners see homeowners.
     */
    public List<User> getSwipeableProfiles(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Determine the opposite role
        User.Role targetRole = (currentUser.getRole() == User.Role.HOMEOWNER)
                ? User.Role.CLEANER
                : User.Role.HOMEOWNER;

        // IDs already swiped by this user
        List<Long> alreadySwiped = swipeRepository.findBySwiperId(userId)
                .stream()
                .map(Swipe::getSwipedId)
                .toList();

        // Get all users with the target role
        List<User> candidates = userRepository.findAll().stream()
                .filter(u -> u.getRole() == targetRole)
                .filter(u -> !u.getId().equals(userId))
                .filter(u -> !alreadySwiped.contains(u.getId()))
                .toList();

        return candidates;
    }

    /**
     * Return all matches for the current user, enriched with the other user's info.
     */
    public List<MatchResponse> getMatches(Long userId) {
        List<Match> matches = matchRepository.findAllByUserId(userId);
        List<MatchResponse> result = new ArrayList<>();

        for (Match m : matches) {
            Long otherId = m.getUser1Id().equals(userId) ? m.getUser2Id() : m.getUser1Id();
            User other = userRepository.findById(otherId).orElse(null);
            if (other == null) continue;

            result.add(new MatchResponse(
                    m.getId(),
                    other.getId(),
                    other.getName(),
                    other.getEmail(),
                    other.getRole().name(),
                    other.getProfilePicture(),
                    m.getCreatedAt()
            ));
        }

        return result;
    }
}