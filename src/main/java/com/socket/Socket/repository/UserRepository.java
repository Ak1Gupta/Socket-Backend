package com.socket.Socket.repository;

import com.socket.Socket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findByPhoneNumberIn(List<String> phoneNumbers);
    List<User> findByUsernameIn(List<String> usernames);
} 