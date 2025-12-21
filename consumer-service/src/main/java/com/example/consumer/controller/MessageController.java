package com.example.consumer.controller;

import com.example.consumer.model.MessageData;
import com.example.consumer.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/all")
    public ResponseEntity<List<MessageData>> getAllMessages() {
        List<MessageData> messages = messageRepository.findAll();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getMessageCount() {
        Long total = (long) messageRepository.findAll().size();
        Long processed = messageRepository.countByProcessed(true);
        Long unprocessed = messageRepository.countByProcessed(false);

        return ResponseEntity.ok(Map.of(
                "total", total,
                "processed", processed,
                "unprocessed", unprocessed));
    }

    @GetMapping("/sender/{sender}")
    public ResponseEntity<List<MessageData>> getMessagesBySender(@PathVariable String sender) {
        List<MessageData> messages = messageRepository.findBySender(sender);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MessageData>> getMessagesByCategory(@PathVariable String category) {
        List<MessageData> messages = messageRepository.findByCategory(category);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/processed/{processed}")
    public ResponseEntity<List<MessageData>> getMessagesByProcessed(@PathVariable Boolean processed) {
        List<MessageData> messages = messageRepository.findByProcessed(processed);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<MessageData> allMessages = messageRepository.findAll();
        Long processedCount = messageRepository.countByProcessed(true);
        Long unprocessedCount = messageRepository.countByProcessed(false);

        return ResponseEntity.ok(Map.of(
                "totalMessages", allMessages.size(),
                "processedMessages", processedCount,
                "unprocessedMessages", unprocessedCount,
                "lastMessage", !allMessages.isEmpty() ? allMessages.get(allMessages.size() - 1) : null));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Consumer service is running");
    }
}