package com.socket.Socket.service;

import com.socket.Socket.model.Message;
import com.socket.Socket.model.Group;
import com.socket.Socket.model.User;
import com.socket.Socket.repository.MessageRepository;
import com.socket.Socket.repository.GroupRepository;
import com.socket.Socket.repository.UserRepository;
import com.socket.Socket.repository.MessageBatchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import com.socket.Socket.model.MessageBatch;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final int BATCH_SIZE = 10;
    
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MessageBatchRepository messageBatchRepository;
    private final ObjectMapper objectMapper;
    
    public MessageService(MessageRepository messageRepository, 
                         GroupRepository groupRepository,
                         UserRepository userRepository,
                         MessageBatchRepository messageBatchRepository) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.messageBatchRepository = messageBatchRepository;
        
        // Configure ObjectMapper with JSR310 module
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    private void checkAndProcessBatch(Group group) {
        try {
            long messageCount = messageRepository.countByGroup(group);
            
            if (messageCount >= BATCH_SIZE) {
                logger.info("Found {} messages for group {}, creating batch", messageCount, group.getId());
                
                // Get the oldest BATCH_SIZE messages for this group
                List<Message> messages = messageRepository.findOldestMessages(group);
                
                if (!messages.isEmpty()) {
                    Integer lastBatchNumber = messageBatchRepository.findMaxBatchNumberByGroup(group);
                    int newBatchNumber = (lastBatchNumber == null) ? 1 : lastBatchNumber + 1;
                    
                    MessageBatch batch = new MessageBatch();
                    batch.setGroup(group);
                    batch.setCreatedAt(LocalDateTime.now());
                    batch.setBatchNumber(newBatchNumber);
                    batch.setMessages(objectMapper.writeValueAsString(messages));
                    
                    // Save batch first
                    messageBatchRepository.save(batch);
                    
                    // Then delete the messages that were batched
                    messageRepository.deleteAll(messages);
                    
                    logger.info("Created batch {} with {} messages for group {}", 
                        newBatchNumber, messages.size(), group.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error processing message batch: ", e);
        }
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
            
            checkAndProcessBatch(savedMessage.getGroup());
            
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
            
            if (!group.getMembers().contains(user)) {
                throw new Exception("User is not a member of this group");
            }
            
            List<Map<String, Object>> allMessages = new ArrayList<>();
            
            // Calculate total messages (both current and batched)
            long currentMessagesCount = messageRepository.countByGroup(group);
            long batchCount = messageBatchRepository.countByGroup(group);
            long totalMessages = currentMessagesCount + (batchCount * BATCH_SIZE);
            
            logger.info("Page: {}, Current messages: {}, Batch count: {}, Total: {}", 
                page, currentMessagesCount, batchCount, totalMessages);
            
            // For page 1, get current messages first
            if (page == 1) {
                if (currentMessagesCount > 0) {
                    List<Message> currentMessages = messageRepository.findByGroupOrderBySentAtDesc(
                        group
//                            ,
//                        PageRequest.of(0, Math.min(limit, (int)currentMessagesCount))
                    );
                    allMessages.addAll(convertMessagesToDTO(currentMessages));
                    logger.info("Added {} current messages", currentMessages.size());
                }
                
                // If we need more messages for page 1, get from most recent batch
                if (allMessages.size() < limit && batchCount > 0) {
                    List<MessageBatch> batches = messageBatchRepository.findByGroupOrderByBatchNumberDesc(
                        group,
                        PageRequest.of(0, 1)
                    );
                    
                    if (!batches.isEmpty()) {
                        MessageBatch batch = batches.get(0);
                        List<Message> batchMessages = objectMapper.readValue(
                            batch.getMessages(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class)
                        );
                        batchMessages.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));
                        allMessages.addAll(convertMessagesToDTO(batchMessages));
                        logger.info("Added {} messages from most recent batch {}", 
                            batchMessages.size(), batch.getBatchNumber());
                    }
                }
            } else {
                // For subsequent pages, calculate which batch to fetch
                int batchesNeeded = (page - 1);
                if (currentMessagesCount > 0) batchesNeeded--;
                
                if (batchesNeeded >= 0 && batchCount > 0) {
                    List<MessageBatch> batches = messageBatchRepository.findByGroupOrderByBatchNumberDesc(
                        group,
                        PageRequest.of(batchesNeeded, 1)
                    );
                    
                    if (!batches.isEmpty()) {
                        MessageBatch batch = batches.get(0);
                        List<Message> batchMessages = objectMapper.readValue(
                            batch.getMessages(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class)
                        );
                        batchMessages.sort((a, b) -> b.getSentAt().compareTo(a.getSentAt()));
                        allMessages.addAll(convertMessagesToDTO(batchMessages));
                        logger.info("Added {} messages from batch {} for page {}", 
                            batchMessages.size(), batch.getBatchNumber(), page);
                    }
                }
            }
            
            // Sort all messages by timestamp
            allMessages.sort((a, b) -> {
                LocalDateTime timeA = LocalDateTime.parse((String)a.get("timestamp"));
                LocalDateTime timeB = LocalDateTime.parse((String)b.get("timestamp"));
                return timeB.compareTo(timeA);
            });
            
            // Take only the required number of messages
            int toIndex = Math.min(limit, allMessages.size());
            List<Map<String, Object>> paginatedMessages = 
                allMessages.subList(0, toIndex);
            
            // Calculate if there are more messages
            boolean hasMore;
            if (page == 1) {
                hasMore = (currentMessagesCount > limit) || (batchCount > 0 && allMessages.size() >= limit);
            } else {
                int batchesNeeded = page - (currentMessagesCount > 0 ? 1 : 0);
                hasMore = batchCount > batchesNeeded;
            }
            
            logger.info("Returning {} messages, hasMore: {}, page: {}", 
                paginatedMessages.size(), hasMore, page);
            
            Map<String, Object> response = new HashMap<>();
            response.put("messages", paginatedMessages);
            response.put("hasMore", hasMore);
            response.put("totalMessages", totalMessages);
            response.put("currentPage", page);
            
            return response;
        } catch (Exception e) {
            logger.error("Error fetching group messages: ", e);
            throw e;
        }
    }
    
    private List<Map<String, Object>> convertMessagesToDTO(List<Message> messages) {
        return messages.stream()
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
    }
} 