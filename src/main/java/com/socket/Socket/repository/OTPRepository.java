package com.socket.Socket.repository;

import com.socket.Socket.model.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTP, Long> {
    @Modifying
    @Query(value = "DELETE FROM public.otps WHERE phone_number = :phoneNumber AND is_verified = false", nativeQuery = true)
    void deleteByPhoneNumberAndIsVerifiedFalse(@Param("phoneNumber") String phoneNumber);
    
    @Query(value = "SELECT * FROM public.otps WHERE phone_number = :phoneNumber AND is_verified = false ORDER BY expiry_time DESC LIMIT 1", nativeQuery = true)
    Optional<OTP> findTopByPhoneNumberAndIsVerifiedFalseOrderByExpiryTimeDesc(@Param("phoneNumber") String phoneNumber);
    
    @Query(value = "SELECT * FROM public.otps WHERE phone_number = :phoneNumber ORDER BY expiry_time DESC LIMIT 1", nativeQuery = true)
    Optional<OTP> findTopByPhoneNumberOrderByExpiryTimeDesc(@Param("phoneNumber") String phoneNumber);
} 