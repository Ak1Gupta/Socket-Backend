package com.socket.Socket.controller;

import com.socket.Socket.model.Message;
import com.socket.Socket.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class MessageController {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final MessageService messageService;
    
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            Long groupId = Long.parseLong(request.get("groupId"));
            String senderUsername = request.get("senderUsername");
            
            Message message = messageService.sendMessage(content, groupId, senderUsername);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupMessages(@PathVariable Long groupId) {
        try {
            List<Message> messages = messageService.getGroupMessages(groupId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error fetching messages: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 