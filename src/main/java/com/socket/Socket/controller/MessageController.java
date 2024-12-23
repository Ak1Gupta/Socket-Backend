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
    
    private final MessageService messageService;
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam String username) {
        try {
            List<Map<String, Object>> messages = messageService.getGroupMessages(groupId, username);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error fetching messages: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 