package com.socket.Socket.service.impl;

import com.socket.Socket.service.SMSService;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TwilioSMSService implements SMSService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioSMSService.class);
    
    @Value("${twilio.account.sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token}")
    private String authToken;
    
    @Value("${twilio.verify.service.sid}")
    private String verifyServiceSid;

    @Override
    public void sendOTP(String phoneNumber) throws Exception {
        try {
            logger.info("Initializing Twilio with SID: {}", accountSid);
            logger.info("Attempting to send OTP to: +91{}", phoneNumber);
            
            Twilio.init(accountSid, authToken);
            
            Verification verification = Verification.creator(
                verifyServiceSid,
                "+91" + phoneNumber,
                "sms"
            ).create();
            
            logger.info("Verification status: {}", verification.getStatus());
            
            if (!"pending".equals(verification.getStatus())) {
                throw new Exception("Failed to send verification code. Status: " + verification.getStatus());
            }
        } catch (Exception e) {
            logger.error("Error sending OTP: {}", e.getMessage(), e);
            throw new Exception("Failed to send OTP: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyOTP(String phoneNumber, String code) throws Exception {
        try {
            Twilio.init(accountSid, authToken);
            
            logger.info("Verifying OTP for phone: {}, code: {}", phoneNumber, code);
            
            VerificationCheck verificationCheck = VerificationCheck.creator(
                verifyServiceSid
            )
            .setTo("+91" + phoneNumber)
            .setCode(code)
            .create();
            
            logger.info("Verification status: {}", verificationCheck.getStatus());
            
            return "approved".equals(verificationCheck.getStatus());
        } catch (Exception e) {
            logger.error("Error verifying OTP: {}", e.getMessage(), e);
            throw e;
        }
    }
} 