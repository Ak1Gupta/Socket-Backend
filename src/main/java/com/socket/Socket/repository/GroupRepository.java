package com.socket.Socket.repository;

import com.socket.Socket.model.Group;
import com.socket.Socket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("SELECT g FROM Group g WHERE g.createdBy = :user OR :user MEMBER OF g.members")
    List<Group> findAllGroupsByUser(@Param("user") User user);
    
    boolean existsByNameAndCreatedBy(String name, User createdBy);
}