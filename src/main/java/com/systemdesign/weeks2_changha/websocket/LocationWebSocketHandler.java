package com.systemdesign.weeks2_changha.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class LocationWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(LocationWebSocketHandler.class);

	private final LocationSessionRegistry sessionRegistry;

	public LocationWebSocketHandler(LocationSessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessionRegistry.register(session);
		log.info("Location ws connected: sessionId={}, activeSessions={}", session.getId(), sessionRegistry.sessionCount());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		sessionRegistry.broadcastToOthers(session.getId(), message.getPayload());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		sessionRegistry.unregister(session.getId());
		log.warn("Transport error for session {}", session.getId(), exception);
		if (session.isOpen()) {
			session.close(CloseStatus.SERVER_ERROR);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessionRegistry.unregister(session.getId());
		log.info("Location ws closed: sessionId={}, status={}", session.getId(), status);
	}
}
