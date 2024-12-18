package com.socket.Socket.service;

import com.socket.Socket.model.OTP;
import com.socket.Socket.repository.OTPRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final OTPRepository otpRepository;
    private final SMSService smsService;
    
    public AuthService(OTPRepository otpRepository, SMSService smsService) {
        this.otpRepository = otpRepository;
        this.smsService = smsService;
    }
    
    @Transactional
    public void sendOTP(String phoneNumber) throws Exception {
        logger.info("Starting OTP send process for phone number: {}", phoneNumber);
        
        try {
            // Delete any existing verification records
            otpRepository.deleteByPhoneNumberAndIsVerifiedFalse(phoneNumber);
            
            // Create a new verification record
            OTP otpEntity = new OTP();
            otpEntity.setPhoneNumber(phoneNumber);
            otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            otpEntity.setVerified(false);
            otpRepository.save(otpEntity);
            
            // Send OTP via Twilio
            smsService.sendOTP(phoneNumber);
            logger.info("OTP sent successfully");
        } catch (Exception e) {
            logger.error("Failed to process OTP request: {}", e.getMessage());
            throw new Exception("Failed to send OTP: " + e.getMessage());
        }
    }
    
    @Transactional
    public boolean verifyOTP(String phoneNumber, String code) throws Exception {
        logger.info("Verifying OTP for phone number: {}", phoneNumber);
        
        try {
            boolean isValid = smsService.verifyOTP(phoneNumber, code);
            
            if (isValid) {
                otpRepository.findTopByPhoneNumberAndIsVerifiedFalseOrderByExpiryTimeDesc(phoneNumber)
                    .ifPresent(otp -> {
                        otp.setVerified(true);
                        otpRepository.save(otp);
                        logger.info("OTP verification status updated for phone: {}", phoneNumber);
                    });
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying OTP: {}", e.getMessage());
            throw e;
        }
    }
} 