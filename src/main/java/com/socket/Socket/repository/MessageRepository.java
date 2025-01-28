package com.socket.Socket.repository;

import com.socket.Socket.model.Message;
import com.socket.Socket.model.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupOrderBySentAtDesc(Group group, Pageable pageable);
    
    // Replace the problematic method with either:
    // Option 1: Using method name
    List<Message> findFirstByGroupOrderBySentAtAsc(Group group, Pageable pageable);
    
    // OR Option 2: Using @Query annotation (preferred)
    @Query("SELECT m FROM Message m WHERE m.group = :group ORDER BY m.sentAt ASC")
    List<Message> findOldestMessages(@Param("group") Group group, Pageable pageable);
    
    void deleteByGroup(Group group);
    long countByGroup(Group group);
}