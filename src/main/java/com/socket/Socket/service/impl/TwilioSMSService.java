package com.socket.Socket.service.impl;

import com.socket.Socket.config.TwilioConfig;
import com.socket.Socket.service.SMSService;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.Safelist;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TwilioSMSService implements SMSService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioSMSService.class);
    
    private final TwilioConfig twilioConfig;
    
    public TwilioSMSService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @Override
    public void sendOTP(String phoneNumber) throws Exception {
        try {
            logger.info("Initializing Twilio with Account SID: {}", twilioConfig.getAccountSid());
            
            if (twilioConfig.getAccountSid() == null || twilioConfig.getAccountSid().isEmpty()) {
                throw new Exception("Twilio Account SID is not configured");
            }
            
            if (twilioConfig.getAuthToken() == null || twilioConfig.getAuthToken().isEmpty()) {
                throw new Exception("Twilio Auth Token is not configured");
            }
            
            if (twilioConfig.getVerifyServiceSid() == null || twilioConfig.getVerifyServiceSid().isEmpty()) {
                throw new Exception("Twilio Verify Service SID is not configured");
            }

            Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());

//            Safelist safelist = Safelist.creator("+918200550414").create();

            System.out.println("SERVICE IDD: " + twilioConfig.getVerifyServiceSid());
            
            Verification verification = Verification.creator(
                twilioConfig.getVerifyServiceSid(),
                "+91" + phoneNumber,
                "sms"
            ).create();
            
            logger.info("Verification status: {}", verification.getStatus());
            
            if (!"pending".equals(verification.getStatus())) {
                throw new Exception("Failed to send verification code. Status: " + verification.getStatus());
            }
        } catch (Exception e) {
            logger.error("Error sending OTP: {}", e.getMessage());
            throw new Exception("Failed to send OTP: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyOTP(String phoneNumber, String code) throws Exception {
        try {
            Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
            
            logger.info("Verifying OTP for phone: {}, code: {}", phoneNumber, code);
            
            VerificationCheck verificationCheck = VerificationCheck.creator(
                twilioConfig.getVerifyServiceSid()
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