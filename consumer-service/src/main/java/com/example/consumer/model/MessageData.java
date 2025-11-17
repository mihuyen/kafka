package com.example.consumer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "received_messages")
public class MessageData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId;
    
    @Column(name = "message_id")
    private Long id;
    
    @Column(name = "content", length = 1000)
    private String content;
    
    @Column(name = "sender")
    private String sender;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @Column(name = "processed")
    private Boolean processed = false;
    
    public MessageData() {
        this.receivedAt = LocalDateTime.now();
    }
    
    public MessageData(Long id, String content, String sender, String category) {
        this.id = id;
        this.content = content;
        this.sender = sender;
        this.category = category;
        this.receivedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "dbId=" + dbId +
                ", id=" + id +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp=" + timestamp +
                ", category='" + category + '\'' +
                ", receivedAt=" + receivedAt +
                ", processed=" + processed +
                '}';
    }
}