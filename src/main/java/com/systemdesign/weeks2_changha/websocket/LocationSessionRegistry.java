package com.systemdesign.weeks2_changha.websocket;

import com.systemdesign.weeks2_changha.friend.FriendRelationService;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

@Component
public class LocationSessionRegistry {

	private final BackpressureProperties properties;
	private final FriendRelationService friendRelationService;
	private final Map<String, SessionBinding> sessionsById = new ConcurrentHashMap<>();
	private final Map<String, Set<String>> sessionIdsByUserId = new ConcurrentHashMap<>();

	public LocationSessionRegistry(BackpressureProperties properties, FriendRelationService friendRelationService) {
		this.properties = properties;
		this.friendRelationService = friendRelationService;
	}

	public void register(WebSocketSession session) {
		String userId = extractUserId(session);
		OutboundSessionDispatcher dispatcher = new OutboundSessionDispatcher(session, properties);
		sessionsById.put(session.getId(), new SessionBinding(userId, dispatcher));
		sessionIdsByUserId.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(session.getId());
	}

	public void unregister(String sessionId) {
		SessionBinding binding = sessionsById.remove(sessionId);
		if (binding == null) {
			return;
		}
		binding.dispatcher().shutdown();
		sessionIdsByUserId.computeIfPresent(binding.userId(), (ignored, sessionIds) -> {
			sessionIds.remove(sessionId);
			return sessionIds.isEmpty() ? null : sessionIds;
		});
	}

	public void broadcastToFriends(String senderSessionId, String payload) {
		SessionBinding sender = sessionsById.get(senderSessionId);
		if (sender == null) {
			return;
		}
		Set<String> friendUserIds = friendRelationService.findFriendIds(sender.userId());
		friendUserIds.forEach(friendUserId -> {
			Set<String> friendSessionIds = sessionIdsByUserId.getOrDefault(friendUserId, Collections.emptySet());
			friendSessionIds.forEach(friendSessionId -> {
				SessionBinding friend = sessionsById.get(friendSessionId);
				if (friend != null) {
					friend.dispatcher().enqueue(payload);
				}
			});
		});
	}

	public int sessionCount() {
		return sessionsById.size();
	}

	private String extractUserId(WebSocketSession session) {
		Object rawUserId = session.getAttributes().get(WsSessionAttributes.USER_ID);
		if (rawUserId instanceof String userId && StringUtils.hasText(userId)) {
			return userId;
		}
		throw new IllegalStateException("WebSocket session is missing authenticated userId");
	}

	private record SessionBinding(String userId, OutboundSessionDispatcher dispatcher) {
	}
}
