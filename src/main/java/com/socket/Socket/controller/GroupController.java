package com.socket.Socket.controller;

import com.socket.Socket.model.Group;
import com.socket.Socket.model.User;
import com.socket.Socket.service.GroupService;
import com.socket.Socket.service.MessageService;
import com.socket.Socket.service.UserService;
import com.socket.Socket.websocket.WebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin
public class GroupController {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);
    private final GroupService groupService;
    private final MessageService messageService;
    private final UserService userService;
    private final WebSocketHandler webSocketHandler;
    
    public GroupController(GroupService groupService, MessageService messageService, UserService userService, WebSocketHandler webSocketHandler) {
        this.groupService = groupService;
        this.messageService = messageService;
        this.userService = userService;
        this.webSocketHandler = webSocketHandler;
    }
    
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String creatorUsername = request.get("creatorUsername");
            
            if (name == null || creatorUsername == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name and creator username are required"));
            }
            
            Group group = groupService.createGroup(name, creatorUsername);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            logger.error("Error creating group: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserGroups(@PathVariable String username) {
        try {
            List<Group> groups = groupService.getUserGroups(username);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Error fetching user groups: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupById(@PathVariable Long groupId) {
        try {
            Group group = groupService.getGroupById(groupId);
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            logger.error("Error fetching group: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMembers(@PathVariable Long groupId, @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> usernames = (List<String>) request.get("usernames");
            String addedBy = (String) request.get("addedBy");
            
            Group group = groupService.addMembersToGroup(groupId, usernames, addedBy);
            
            // Create system message
            String systemMessage = String.format("%s added %s to the group",
                addedBy,
                String.join(", ", usernames)
            );
            
            // Only broadcast - it will handle saving internally
            webSocketHandler.broadcastSystemMessage(groupId, systemMessage);
            
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            logger.error("Error adding members: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/check-contacts")
    public ResponseEntity<?> checkContacts(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> phoneNumbers = request.get("phoneNumbers");
            if (phoneNumbers == null || phoneNumbers.isEmpty()) {
                return ResponseEntity.ok(Map.of("users", List.of()));
            }
            
            List<User> registeredUsers = userService.findRegisteredUsers(phoneNumbers);
            return ResponseEntity.ok(Map.of("users", registeredUsers));
        } catch (Exception e) {
            logger.error("Error checking contacts: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting group: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 