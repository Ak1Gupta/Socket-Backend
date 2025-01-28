package com.socket.Socket.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "message_batches", schema = "public")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(columnDefinition = "TEXT")
    private String messages; // JSON string of messages
    
    @Column(nullable = false)
    private Integer batchNumber;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }
} 