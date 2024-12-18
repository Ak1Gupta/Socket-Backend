package com.socket.Socket.service;

import com.socket.Socket.model.Group;
import com.socket.Socket.model.User;
import com.socket.Socket.repository.GroupRepository;
import com.socket.Socket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public Group createGroup(String name, String creatorUsername) throws Exception {
        User creator = userRepository.findByUsername(creatorUsername)
            .orElseThrow(() -> new Exception("User not found"));
            
        if (groupRepository.existsByNameAndCreatedBy(name, creator)) {
            throw new Exception("You already have a group with this name");
        }
        
        Group group = new Group();
        group.setName(name);
        group.setCreatedBy(creator);
        group.setCreatedAt(LocalDateTime.now());
        group.getMembers().add(creator); // Add creator as a member
        
        return groupRepository.save(group);
    }
    
    public List<Group> getUserGroups(String username) throws Exception {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new Exception("User not found"));
            
        return groupRepository.findAllGroupsByUser(user);
    }
    
    public Group getGroupById(Long groupId) throws Exception {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new Exception("Group not found"));
    }
    
    @Transactional
    public Group addMemberToGroup(Long groupId, String username) throws Exception {
        Group group = getGroupById(groupId);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new Exception("User not found"));
            
        if (group.getMembers().contains(user)) {
            throw new Exception("User is already a member of this group");
        }
        
        group.getMembers().add(user);
        return groupRepository.save(group);
    }
    
    @Transactional
    public Group removeMemberFromGroup(Long groupId, String username) throws Exception {
        Group group = getGroupById(groupId);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new Exception("User not found"));
            
        if (user.equals(group.getCreatedBy())) {
            throw new Exception("Cannot remove the group creator");
        }
        
        if (!group.getMembers().contains(user)) {
            throw new Exception("User is not a member of this group");
        }
        
        group.getMembers().remove(user);
        return groupRepository.save(group);
    }
}