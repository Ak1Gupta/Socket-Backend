package com.socket.Socket.service;

import com.socket.Socket.model.Message;
import com.socket.Socket.model.Group;
import com.socket.Socket.model.User;
import com.socket.Socket.repository.MessageRepository;
import com.socket.Socket.repository.GroupRepository;
import com.socket.Socket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    
    public MessageService(MessageRepository messageRepository, 
                         GroupRepository groupRepository,
                         UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public Message sendMessage(String content, Long groupId, String senderUsername) throws Exception {
        User sender = userRepository.findByUsername(senderUsername)
            .orElseThrow(() -> new Exception("User not found"));
            
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new Exception("Group not found"));
            
        if (!group.getMembers().contains(sender)) {
            throw new Exception("User is not a member of this group");
        }
        
        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        message.setGroup(group);
        message.setSentAt(LocalDateTime.now());
        
        return messageRepository.save(message);
    }
    
    public List<Message> getGroupMessages(Long groupId) throws Exception {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new Exception("Group not found"));
            
        return messageRepository.findByGroupOrderBySentAtDesc(group);
    }
} 