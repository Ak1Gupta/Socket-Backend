package com.socket.Socket.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otps", schema = "public")
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;
    
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
    
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public OTP() {
    }

    public OTP(String phoneNumber, LocalDateTime expiryTime) {
        this.phoneNumber = phoneNumber;
        this.expiryTime = expiryTime;
        this.isVerified = false;
    }
}