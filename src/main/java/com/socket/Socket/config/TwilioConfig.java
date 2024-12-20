package com.socket.Socket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {
    @Value("${TWILIO_ACCOUNT_SID}")
    private String accountSid;
    
    @Value("${TWILIO_AUTH_TOKEN}")
    private String authToken;
    
    @Value("${TWILIO_VERIFY_SERVICE_SID}")
    private String verifyServiceSid;

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