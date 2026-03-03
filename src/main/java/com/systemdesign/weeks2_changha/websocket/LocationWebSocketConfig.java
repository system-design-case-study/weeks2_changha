package com.systemdesign.weeks2_changha.websocket;

import com.systemdesign.weeks2_changha.friend.FriendRelationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableConfigurationProperties({BackpressureProperties.class, FriendRelationProperties.class})
public class LocationWebSocketConfig implements WebSocketConfigurer {

	private final LocationWebSocketHandler handler;
	private final WsUserHandshakeInterceptor handshakeInterceptor;

	public LocationWebSocketConfig(
		LocationWebSocketHandler handler,
		WsUserHandshakeInterceptor handshakeInterceptor
	) {
		this.handler = handler;
		this.handshakeInterceptor = handshakeInterceptor;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/ws/location")
			.addInterceptors(handshakeInterceptor)
			.setAllowedOriginPatterns("*");
	}
}
