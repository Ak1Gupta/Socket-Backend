package com.socket.Socket.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.socket.Socket.model.Group;
import com.socket.Socket.model.Message;
import com.socket.Socket.model.User;
import com.socket.Socket.service.GroupService;
import com.socket.Socket.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionToUser = new ConcurrentHashMap<>(); // sessionId -> username
    private static final Map<String, Long> sessionToGroup = new ConcurrentHashMap<>(); // sessionId -> groupId
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final GroupService groupService;

    public WebSocketHandler(MessageService messageService, GroupService groupService) {
        this.messageService = messageService;
        this.groupService = groupService;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract groupId and username from URL parameters
        String query = session.getUri().getQuery();
        Map<String, String> params = parseQueryString(query);
        
        String username = params.get("username");
        Long groupId = Long.parseLong(params.get("groupId"));
        
        // Store session mappings
        sessions.put(session.getId(), session);
        sessionToUser.put(session.getId(), username);
        sessionToGroup.put(session.getId(), groupId);
        
        logger.info("User {} connected to group {}", username, groupId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.get("type").asText();
            if(type.equals("JOIN")){
                return ;
            }
            Long groupId = jsonNode.get("groupId").asLong();
            String sender = jsonNode.get("sender").asText();

            logger.info("Received message - Type: {}, GroupId: {}, Sender: {}", type, groupId, sender);

            // Save message to database first
            Message savedMessage = messageService.saveMessage(jsonNode);
            logger.info("Message saved to database with ID: {}", savedMessage.getId());

            // Create message response with consistent structure
            Map<String, Object> messageResponse = new HashMap<>();
            messageResponse.put("id", savedMessage.getId().toString());
            messageResponse.put("content", savedMessage.getContent());
            messageResponse.put("sender", sender);
            messageResponse.put("type", savedMessage.getType());
            messageResponse.put("timestamp", savedMessage.getSentAt().toString());
            messageResponse.put("groupId", groupId);

            // Send formatted message only to users in the same group
            TextMessage formattedMessage = new TextMessage(objectMapper.writeValueAsString(messageResponse));
            
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession userSession = entry.getValue();
                Long userGroupId = sessionToGroup.get(entry.getKey());
                
                if (userSession.isOpen() && groupId.equals(userGroupId)) {
                    userSession.sendMessage(formattedMessage);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling message: ", e);
            throw e;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = sessionToUser.remove(session.getId());
        Long groupId = sessionToGroup.remove(session.getId());
        sessions.remove(session.getId());
        logger.info("User {} disconnected from group {}", username, groupId);
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }

    // Method to broadcast system messages
    public void broadcastSystemMessage(Long groupId, String content) {
        try {
            // Get group and its members
            Group group = groupService.getGroupById(groupId);
            Set<String> groupMemberUsernames = group.getMembers().stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("id", UUID.randomUUID().toString());
            systemMessage.put("type", "SYSTEM");
            systemMessage.put("content", content);
            systemMessage.put("timestamp", LocalDateTime.now().toString());
            systemMessage.put("groupId", groupId);
            systemMessage.put("sender", null);
            
            // Save to database first
            messageService.saveMessage(objectMapper.valueToTree(systemMessage));
            
            // Then broadcast only to group members
            String messageJson = objectMapper.writeValueAsString(systemMessage);
            TextMessage textMessage = new TextMessage(messageJson);
            
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession userSession = entry.getValue();
                String sessionUsername = sessionToUser.get(entry.getKey());
                
                if (userSession.isOpen() && groupMemberUsernames.contains(sessionUsername)) {
                    userSession.sendMessage(textMessage);
                }
            }
        } catch (Exception e) {
            logger.error("Error broadcasting system message: ", e);
        }
    }
} 