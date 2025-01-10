package com.socket.Socket.service;

import com.socket.Socket.model.Message;
import com.socket.Socket.model.Group;
import com.socket.Socket.model.User;
import com.socket.Socket.repository.MessageRepository;
import com.socket.Socket.repository.GroupRepository;
import com.socket.Socket.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    
    public MessageService(MessageRepository messageRepository, 
                         GroupRepository groupRepository,
                         UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public Message saveMessage(JsonNode jsonNode) throws Exception {
        try {
            String type = jsonNode.get("type").asText();
            Long groupId = jsonNode.get("groupId").asLong();
            String content = jsonNode.get("content").asText();
            
            logger.info("Saving message - Type: {}, GroupId: {}, Content: {}", type, groupId, content);
            
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));
                
            Message message = new Message();
            message.setContent(content);
            message.setGroup(group);
            message.setSentAt(LocalDateTime.now());
            message.setType(type);
            
            if ("CHAT".equals(type)) {
                String senderUsername = jsonNode.get("sender").asText();
                logger.info("Message sender: {}", senderUsername);
                
                User sender = userRepository.findByUsername(senderUsername)
                    .orElseThrow(() -> new Exception("User not found"));
                    
                if (!group.getMembers().contains(sender)) {
                    throw new Exception("User is not a member of this group");
                }
                
                message.setSender(sender);
            } else {
                message.setSender(null);
                message.setRead(true);
            }
            
            Message savedMessage = messageRepository.save(message);
            logger.info("Message saved successfully with ID: {}", savedMessage.getId());
            return savedMessage;
        } catch (Exception e) {
            logger.error("Error saving message: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public Message sendMessage(String content, Long groupId, String senderUsername) throws Exception {
        User sender = userRepository.findByUsername(senderUsername)
            .orElseThrow(() -> new Exception("User not found"));
            
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new Exception("Group not found"));
            
        if (!group.getMembers().contains(sender)) {
            throw new Exception("User is not a member of this group");
        }
        
        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        message.setGroup(group);
        message.setSentAt(LocalDateTime.now());
        
        return messageRepository.save(message);
    }
    
    @Transactional
    public Message sendSystemMessage(Long groupId, String content) throws Exception {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new Exception("Group not found"));
            
        Message message = new Message();
        message.setContent(content);
        message.setGroup(group);
        message.setSentAt(LocalDateTime.now());
        message.setType("SYSTEM");
        message.setSender(null);
        message.setRead(true);
        
        return messageRepository.save(message);
    }
    
    public Map<String, Object> getGroupMessages(Long groupId, String username, int page, int limit) throws Exception {
        try {
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));
                
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("User not found"));
                
            // Verify user is member of the group
            if (!group.getMembers().contains(user)) {
                throw new Exception("User is not a member of this group");
            }
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page - 1, limit);
            
            // Get total count of messages
            long totalMessages = messageRepository.countByGroup(group);
            
            logger.info("Fetching messages for group: {}, user: {}, page: {}, limit: {}", 
                groupId, username, page, limit);
                
            List<Message> messages = messageRepository.findByGroupOrderBySentAtDesc(group, pageable);
            
            // Convert messages to DTOs
            List<Map<String, Object>> messageDTOs = messages.stream()
                .map(message -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", message.getId());
                    dto.put("content", message.getContent());
                    dto.put("sender", message.getSender() != null ? message.getSender().getUsername() : null);
                    dto.put("type", message.getType());
                    dto.put("timestamp", message.getSentAt().toString());
                    dto.put("groupId", message.getGroup().getId());
                    return dto;
                })
                .collect(Collectors.toList());
            
            // Calculate pagination metadata
            int totalPages = (int) Math.ceil((double) totalMessages / limit);
            boolean hasMore = page * limit < totalMessages;
            
            // Create response map
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messageDTOs);
            response.put("hasMore", hasMore);
            response.put("total", totalMessages);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            
            logger.info("Found {} messages, total: {}, hasMore: {}", 
                messages.size(), totalMessages, hasMore);
                
            return response;
        } catch (Exception e) {
            logger.error("Error fetching group messages: ", e);
            throw e;
        }
    }
} 