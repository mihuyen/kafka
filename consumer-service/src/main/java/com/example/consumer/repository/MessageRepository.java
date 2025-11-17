package com.example.consumer.repository;

import com.example.consumer.model.MessageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageData, Long> {
    
    List<MessageData> findBySender(String sender);
    
    List<MessageData> findByCategory(String category);
    
    List<MessageData> findByProcessed(Boolean processed);
    
    @Query("SELECT m FROM MessageData m WHERE m.receivedAt BETWEEN ?1 AND ?2")
    List<MessageData> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(m) FROM MessageData m WHERE m.processed = true")
    Long countProcessedMessages();
    
    @Query("SELECT COUNT(m) FROM MessageData m WHERE m.processed = false")
    Long countUnprocessedMessages();
}