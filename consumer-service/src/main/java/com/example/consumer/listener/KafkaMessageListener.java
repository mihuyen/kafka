package com.example.consumer.listener;

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

import java.time.LocalDateTime;

@Service
public class KafkaMessageListener {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageListener.class);
    
    @Autowired
    private MessageRepository messageRepository;
    
    @KafkaListener(
        topics = "message-topic",
        groupId = "message-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(@Payload MessageData message,
                      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                      @Header(KafkaHeaders.OFFSET) long offset,
                      @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        try {
            logger.info("Received message: {} from partition: {} with offset: {}", 
                message, partition, offset);
            
            // Set received timestamp
            message.setReceivedAt(LocalDateTime.now());
            message.setProcessed(true);
            
            // Save to database
            messageRepository.save(message);
            
            logger.info("Message saved successfully with ID: {}", message.getId());
        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
            // Continue processing other messages even if one fails
        }
    }
}