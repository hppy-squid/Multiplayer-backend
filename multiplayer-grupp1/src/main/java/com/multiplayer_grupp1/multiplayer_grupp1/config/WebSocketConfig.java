package com.multiplayer_grupp1.multiplayer_grupp1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Config för websocket, här vi sätter upp grunden för websocket 
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

    // Våran ws endpoint, tillåter alla originmönster med SockJS
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Specificerar vilken url som logiken hanteras via
        config.setApplicationDestinationPrefixes("/app");
        // Specificerar vilka endpoints vi skickar via 
        config.enableSimpleBroker("/readycheck", "/response", "/timer", "/lobby");
    }

}