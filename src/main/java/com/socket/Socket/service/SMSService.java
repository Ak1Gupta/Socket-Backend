package com.socket.Socket.service;

public interface SMSService {
    void sendOTP(String phoneNumber) throws Exception;
    boolean verifyOTP(String phoneNumber, String code) throws Exception;
} 