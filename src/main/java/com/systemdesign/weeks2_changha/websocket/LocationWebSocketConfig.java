package com.systemdesign.weeks2_changha.websocket;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableConfigurationProperties(BackpressureProperties.class)
public class LocationWebSocketConfig implements WebSocketConfigurer {

	private final LocationWebSocketHandler handler;

	public LocationWebSocketConfig(LocationWebSocketHandler handler) {
		this.handler = handler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/ws/location")
			.setAllowedOriginPatterns("*");
	}
}
