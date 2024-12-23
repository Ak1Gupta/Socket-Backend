package com.socket.Socket.controller;

import com.socket.Socket.model.User;
import com.socket.Socket.service.AuthService;
import com.socket.Socket.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final UserService userService;
    
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOTP(@RequestBody Map<String, String> request) {
        try {

            String phoneNumber = request.get("phoneNumber");

            logger.info("Received OTP request for phone number: {}", phoneNumber);
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Phone number is required"));
            }
            
            authService.sendOTP(phoneNumber);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            logger.error("Error sending OTP: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");
            String code = request.get("code");
            
            logger.info("Received verification request for phone: {}", phoneNumber);
            
            if (phoneNumber == null || code == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Phone number and code are required"));
            }
            
            boolean isValid = authService.verifyOTP(phoneNumber, code);
            return ResponseEntity.ok(Map.of("verified", isValid));
        } catch (Exception e) {
            logger.error("Error verifying OTP: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");
            String username = request.get("username");
            
            // Validate required fields
            if (phoneNumber == null || firstName == null || lastName == null || username == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "All fields are required"));
            }
            
            User user = userService.createUser(phoneNumber, firstName, lastName, username);
            
            return ResponseEntity.ok(Map.of(
                "message", "User created successfully",
                "username", user.getUsername()
            ));
        } catch (Exception e) {
            logger.error("Error in signup: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/check-user/{phoneNumber}")
    public ResponseEntity<?> checkUser(@PathVariable String phoneNumber) {
        try {
            Optional<User> user = userService.findByPhoneNumber(phoneNumber);
            if (user.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "username", user.get().getUsername()
                ));
            } else {
                return ResponseEntity.ok(Map.of("exists", false));
            }
        } catch (Exception e) {
            logger.error("Error checking user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
} 