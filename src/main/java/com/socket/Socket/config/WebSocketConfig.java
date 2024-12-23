package com.socket.Socket.config;

import com.socket.Socket.websocket.WebSocketHandler;
import com.socket.Socket.service.MessageService;
import com.socket.Socket.service.GroupService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageService messageService;
    private final GroupService groupService;

    public WebSocketConfig(MessageService messageService, GroupService groupService) {
        this.messageService = messageService;
        this.groupService = groupService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(messageService, groupService), "/ws")
            .setAllowedOrigins("*");
    }
} 