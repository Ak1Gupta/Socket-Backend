package com.socket.Socket.repository;

import com.socket.Socket.model.MessageBatch;
import com.socket.Socket.model.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MessageBatchRepository extends JpaRepository<MessageBatch, Long> {
    List<MessageBatch> findByGroupOrderByBatchNumberDesc(Group group, Pageable pageable);
    List<MessageBatch> findByGroupOrderByBatchNumberDesc(Group group);
    
    @Query("SELECT MAX(mb.batchNumber) FROM MessageBatch mb WHERE mb.group = ?1")
    Integer findMaxBatchNumberByGroup(Group group);
    
    long countByGroup(Group group);
    
    MessageBatch findByGroupAndBatchNumber(Group group, Integer batchNumber);
} 