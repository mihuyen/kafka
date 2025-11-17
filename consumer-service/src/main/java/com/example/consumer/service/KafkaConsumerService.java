package com.example.consumer.service;

import com.example.consumer.model.MessageData;
import com.example.consumer.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    
    @Autowired
    private MessageRepository messageRepository;
    
    @KafkaListener(topics = "message-topic", groupId = "message-consumer-group")
    public void consume(@Payload MessageData message,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("Received message: {} from topic: {}, partition: {}, offset: {}", 
                   message, topic, partition, offset);
        
        try {
            // Process and save message
            processMessage(message);
            
            // Save to database
            MessageData savedMessage = messageRepository.save(message);
            logger.info("Message saved to database with DB ID: {}", savedMessage.getDbId());
            
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage());
            // In a real application, you might want to send to a dead letter topic
        }
    }
    
    private void processMessage(MessageData message) {
        // Simulate processing logic
        logger.info("Processing message ID: {} from sender: {}", message.getId(), message.getSender());
        
        // Mark as processed
        message.setProcessed(true);
        
        // Add any business logic here
        if ("ERROR".equalsIgnoreCase(message.getCategory())) {
            logger.warn("Error message received: {}", message.getContent());
        } else if ("INFO".equalsIgnoreCase(message.getCategory())) {
            logger.info("Info message processed: {}", message.getContent());
        }
    }
}