package com.socket.Socket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class TwilioConfig {
    private static final Logger logger = LoggerFactory.getLogger(TwilioConfig.class);

    @Value("${TWILIO_ACCOUNT_SIDD}")
    private String accountSid;
    
    @Value("${TWILIO_AUTH_TOKENN}")
    private String authToken;
    
    @Value("${TWILIO_VERIFY_SERVICE_SIDD}")
    private String verifyServiceSid;

    @PostConstruct
    public void init() {
        logger.info("Loaded Twilio config - Account SID: {}, Verify Service SID: {}", 
            accountSid, verifyServiceSid);
        if (accountSid == null || accountSid.isEmpty()) {
            logger.error("TWILIO_ACCOUNT_SID not found in environment variables");
        }
        if (authToken == null || authToken.isEmpty()) {
            logger.error("TWILIO_AUTH_TOKEN not found in environment variables");
        }
        if (verifyServiceSid == null || verifyServiceSid.isEmpty()) {
            logger.error("TWILIO_VERIFY_SERVICE_SID not found in environment variables");
        }
    }

    public String getAccountSid() {
        return accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getVerifyServiceSid() {
        return verifyServiceSid;
    }
} 