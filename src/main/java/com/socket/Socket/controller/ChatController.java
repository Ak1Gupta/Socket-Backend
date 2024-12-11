package com.socket.Socket.controller;

import com.socket.Socket.model.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class ChatController {

    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public Message handleMessage(Message message) {
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    @MessageMapping("/join")
    @SendTo("/topic/messages")
    public Message handleJoin(Message message) {
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setType(Message.MessageType.JOIN);
        return message;
    }
} 