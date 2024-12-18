package com.socket.Socket.repository;

import com.socket.Socket.model.Message;
import com.socket.Socket.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByGroupOrderBySentAtDesc(Group group);
}