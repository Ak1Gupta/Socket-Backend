package com.socket.Socket.repository;

import com.socket.Socket.model.Group;
import com.socket.Socket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByNameAndCreatedBy(String name, User createdBy);
    
    @Query("SELECT g FROM Group g WHERE :user MEMBER OF g.members")
    List<Group> findAllGroupsByUser(@Param("user") User user);
}