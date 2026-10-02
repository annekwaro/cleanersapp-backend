package com.example.Cleanersapp.repository;

import com.example.Cleanersapp.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.user1Id = :userId OR m.user2Id = :userId")
    List<Match> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Match m " +
           "WHERE (m.user1Id = :a AND m.user2Id = :b) OR (m.user1Id = :b AND m.user2Id = :a)")
    boolean existsBetweenUsers(@Param("a") Long a, @Param("b") Long b);
}