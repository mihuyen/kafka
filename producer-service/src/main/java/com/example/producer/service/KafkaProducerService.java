package com.example.producer.service;

import com.example.producer.model.MessageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "message-topic";
    
    @Autowired
    private KafkaTemplate<String, MessageData> kafkaTemplate;
    
    public void sendMessage(MessageData message) {
        try {
            CompletableFuture<SendResult<String, MessageData>> future = 
                kafkaTemplate.send(TOPIC, message.getId().toString(), message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent message=[{}] with offset=[{}]", 
                        message, result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send message=[{}] due to : {}", 
                        message, ex.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error occurred while sending message: {}", e.getMessage());
            throw new RuntimeException("Failed to send message", e);
        }
    }
    
    public void sendMessageWithPartition(MessageData message, Integer partition) {
        try {
            CompletableFuture<SendResult<String, MessageData>> future = 
                kafkaTemplate.send(TOPIC, partition, message.getId().toString(), message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent message=[{}] to partition=[{}] with offset=[{}]", 
                        message, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send message=[{}] due to : {}", 
                        message, ex.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error occurred while sending message to partition: {}", e.getMessage());
            throw new RuntimeException("Failed to send message to partition", e);
        }
    }
}