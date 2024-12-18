package com.socket.Socket.controller;

import com.socket.Socket.model.Group;
import com.socket.Socket.service.GroupService;
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
    
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
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
} 