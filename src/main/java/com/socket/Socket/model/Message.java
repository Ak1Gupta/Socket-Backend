package com.socket.Socket.model;

import java.time.LocalDateTime;

public class Message {
    private String id;
    private String content;

    public Message() {
    }

    public Message(MessageType type, LocalDateTime timestamp, String sender, String content, String id) {
        this.type = type;
        this.timestamp = timestamp;
        this.sender = sender;
        this.content = content;
        this.id = id;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String sender;
    private LocalDateTime timestamp;
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }




}