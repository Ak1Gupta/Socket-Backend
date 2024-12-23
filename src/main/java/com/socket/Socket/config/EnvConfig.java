package com.socket.Socket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@Configuration
@PropertySource("file:.env")
public class EnvConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);

    @PostConstruct
    public void init() {
        logger.info("Loading environment variables from .env file");
    }
}