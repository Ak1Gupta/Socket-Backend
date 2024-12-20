package com.socket.Socket.controller;

import com.socket.Socket.model.User;
import com.socket.Socket.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserDetails(@PathVariable String username) {
        try {
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new Exception("User not found"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user: {}", e.getMessage());
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
} 