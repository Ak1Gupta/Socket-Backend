package com.socket.Socket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.socket.Socket.model.Group;
import com.socket.Socket.model.Message;
import com.socket.Socket.model.User;
import com.socket.Socket.service.GroupService;
import com.socket.Socket.service.MessageService;
import com.socket.Socket.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionToUser = new ConcurrentHashMap<>(); // sessionId -> username
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final GroupService groupService;

    public ChatWebSocketHandler(MessageService messageService, GroupService groupService) {
        this.messageService = messageService;
        this.groupService = groupService;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.get("type").asText();
            Long groupId = jsonNode.get("groupId").asLong();
            String sender = jsonNode.get("sender").asText();

            logger.info("Received message - Type: {}, GroupId: {}, Sender: {}", type, groupId, sender);

            // Store user session mapping
            sessionToUser.put(session.getId(), sender);

            // Get group and its members
            Group group = groupService.getGroupById(groupId);
            Set<String> groupMemberUsernames = group.getMembers().stream()
                    .map(User::getUsername)
                    .collect(Collectors.toSet());

            // Save message to database first
            Message savedMessage = messageService.saveMessage(jsonNode);
            logger.info("Message saved to database with ID: {}", savedMessage.getId());

            // Then broadcast to group members
            for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
                WebSocketSession userSession = entry.getValue();
                String sessionUsername = sessionToUser.get(entry.getKey());

                if (userSession.isOpen() && groupMemberUsernames.contains(sessionUsername)) {
                    userSession.sendMessage(message);
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
        sessions.remove(session.getId());
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
            String messageId = UUID.randomUUID().toString();
            systemMessage.put("id", messageId);
            systemMessage.put("type", "SYSTEM");
            systemMessage.put("content", content);
            systemMessage.put("timestamp", LocalDateTime.now().toString());
            systemMessage.put("groupId", groupId);

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

