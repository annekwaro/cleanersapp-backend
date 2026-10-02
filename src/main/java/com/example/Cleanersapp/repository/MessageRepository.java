package com.example.Cleanersapp.repository;

import com.example.Cleanersapp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByMatchIdOrderBySentAtAsc(Long matchId);
}