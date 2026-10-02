package com.example.Cleanersapp.repository;

import com.example.Cleanersapp.model.Swipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {

    Optional<Swipe> findBySwiperIdAndSwipedId(Long swiperId, Long swipedId);

    List<Swipe> findBySwiperId(Long swiperId);

    List<Swipe> findBySwipedIdAndDirection(Long swipedId, String direction);

    boolean existsBySwiperIdAndSwipedId(Long swiperId, Long swipedId);
}