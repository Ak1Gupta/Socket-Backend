package com.socket.Socket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.socket")
public class SocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocketApplication.class, args);
    }
} 