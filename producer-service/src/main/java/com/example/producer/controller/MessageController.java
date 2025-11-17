package com.example.producer.controller;

import com.example.producer.model.MessageData;
import com.example.producer.service.KafkaProducerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {
    
    @Autowired
    private KafkaProducerService producerService;
    
    private final AtomicLong messageIdGenerator = new AtomicLong(1);
    
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@Valid @RequestBody MessageData message) {
        try {
            message.setId(messageIdGenerator.getAndIncrement());
            producerService.sendMessage(message);
            return ResponseEntity.ok("Message sent successfully with ID: " + message.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed to send message: " + e.getMessage());
        }
    }
    
    @PostMapping("/send/partition/{partition}")
    public ResponseEntity<String> sendMessageToPartition(
            @Valid @RequestBody MessageData message, 
            @PathVariable Integer partition) {
        try {
            message.setId(messageIdGenerator.getAndIncrement());
            producerService.sendMessageWithPartition(message, partition);
            return ResponseEntity.ok(
                "Message sent successfully to partition " + partition + " with ID: " + message.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed to send message to partition: " + e.getMessage());
        }
    }
    
    @PostMapping("/send/bulk")
    public ResponseEntity<String> sendBulkMessages(
            @RequestParam(defaultValue = "10") Integer count,
            @RequestParam(defaultValue = "TestSender") String sender,
            @RequestParam(defaultValue = "INFO") String category) {
        try {
            for (int i = 1; i <= count; i++) {
                MessageData message = new MessageData(
                    messageIdGenerator.getAndIncrement(),
                    "Bulk message " + i + " - " + System.currentTimeMillis(),
                    sender,
                    category
                );
                producerService.sendMessage(message);
            }
            return ResponseEntity.ok("Sent " + count + " messages successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed to send bulk messages: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Producer service is running");
    }
}