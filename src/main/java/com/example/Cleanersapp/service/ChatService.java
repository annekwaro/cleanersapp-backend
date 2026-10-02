package com.example.Cleanersapp.service;

import com.example.Cleanersapp.dto.MessageResponse;
import com.example.Cleanersapp.model.Match;
import com.example.Cleanersapp.model.Message;
import com.example.Cleanersapp.model.User;
import com.example.Cleanersapp.repository.MatchRepository;
import com.example.Cleanersapp.repository.MessageRepository;
import com.example.Cleanersapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Send a message in a match. Only users in the match can send.
     */
    public MessageResponse sendMessage(Long senderId, Long matchId, String content) {
        // Validate content
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        // Validate match exists
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // Validate sender is part of the match
        if (!match.getUser1Id().equals(senderId) && !match.getUser2Id().equals(senderId)) {
            throw new RuntimeException("You are not part of this match");
        }

        // Save message
        Message message = new Message();
        message.setMatchId(matchId);
        message.setSenderId(senderId);
        message.setContent(content.trim());
        messageRepository.save(message);

        User sender = userRepository.findById(senderId).orElseThrow();

        return new MessageResponse(
                message.getId(),
                matchId,
                senderId,
                sender.getName(),
                message.getContent(),
                message.getSentAt(),
                true // the sender is always "me" here
        );
    }

    /**
     * Get all messages for a match. Only users in the match can read.
     */
    public List<MessageResponse> getMessages(Long userId, Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if (!match.getUser1Id().equals(userId) && !match.getUser2Id().equals(userId)) {
            throw new RuntimeException("You are not part of this match");
        }

        List<Message> messages = messageRepository.findByMatchIdOrderBySentAtAsc(matchId);
        List<MessageResponse> result = new ArrayList<>();

        for (Message m : messages) {
            User sender = userRepository.findById(m.getSenderId()).orElse(null);
            String senderName = sender != null ? sender.getName() : "Unknown";

            result.add(new MessageResponse(
                    m.getId(),
                    m.getMatchId(),
                    m.getSenderId(),
                    senderName,
                    m.getContent(),
                    m.getSentAt(),
                    m.getSenderId().equals(userId)
            ));
        }

        return result;
    }
}