package com.systemdesign.weeks2_changha.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class LocationSessionRegistry {

	private final BackpressureProperties properties;
	private final Map<String, OutboundSessionDispatcher> dispatchers = new ConcurrentHashMap<>();

	public LocationSessionRegistry(BackpressureProperties properties) {
		this.properties = properties;
	}

	public void register(WebSocketSession session) {
		dispatchers.put(session.getId(), new OutboundSessionDispatcher(session, properties));
	}

	public void unregister(String sessionId) {
		OutboundSessionDispatcher dispatcher = dispatchers.remove(sessionId);
		if (dispatcher != null) {
			dispatcher.shutdown();
		}
	}

	public void broadcastToOthers(String senderSessionId, String payload) {
		dispatchers.forEach((sessionId, dispatcher) -> {
			if (!sessionId.equals(senderSessionId)) {
				dispatcher.enqueue(payload);
			}
		});
	}

	public int sessionCount() {
		return dispatchers.size();
	}
}
