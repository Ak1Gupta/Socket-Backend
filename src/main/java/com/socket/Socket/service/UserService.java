package com.socket.Socket.service;

import com.socket.Socket.model.User;
import com.socket.Socket.repository.OTPRepository;
import com.socket.Socket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final OTPRepository otpRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public UserService(UserRepository userRepository, OTPRepository otpRepository) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
    }
    
    @Transactional
    public User createUser(String phoneNumber, String firstName, String lastName, String username) throws Exception {
        // Verify that OTP was validated - look for the most recent verified OTP
        boolean isVerified = true;

        //         Without OTP
//                true;
//                With otp
//                otpRepository.findTopByPhoneNumberOrderByExpiryTimeDesc(phoneNumber)
//                .map(otp -> otp.isVerified())
//                .orElse(false);


                
        if (!isVerified) {
            throw new Exception("Phone number not verified");
        }
        
        // Check if username is available
        if (userRepository.existsByUsername(username)) {
            throw new Exception("Username already taken");
        }
        
        // Check if phone number is already registered
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new Exception("Phone number already registered");
        }
        
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setOnline(false);
        user.setStatus("Hey, I'm using Aora!"); // Default status
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public List<User> findRegisteredUsers(List<String> phoneNumbers) {
        try {
            return userRepository.findByPhoneNumberIn(phoneNumbers);
        } catch (Exception e) {
            logger.error("Error finding registered users: {}", e.getMessage());
            return List.of(); // Return empty list instead of throwing
        }
    }
} 