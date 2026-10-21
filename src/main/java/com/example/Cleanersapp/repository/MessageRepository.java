package com.example.Cleanersapp.repository;

import com.example.Cleanersapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByMatchIdOrderBySentAtAsc(Long matchId);

    Optional<Message> findFirstByMatchIdOrderBySentAtDesc(Long matchId);
}