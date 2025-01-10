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
            @RequestParam String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            // Validate and sanitize input
            page = Math.max(1, page);
            limit = Math.min(50, Math.max(1, limit)); // Cap at 50 messages per request
            
            Map<String, Object> response = messageService.getGroupMessages(groupId, username, page, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching messages: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 