package com.systemdesign.weeks2_changha.websocket;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WsUserHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes
	) {
		String userId = extractUserId(request);
		if (!StringUtils.hasText(userId)) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}
		attributes.put(WsSessionAttributes.USER_ID, userId);
		return true;
	}

	@Override
	public void afterHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Exception exception
	) {
		// no-op
	}

	private String extractUserId(ServerHttpRequest request) {
		String queryUserId = UriComponentsBuilder.fromUri(request.getURI())
			.build()
			.getQueryParams()
			.getFirst(WsSessionAttributes.USER_ID);
		if (StringUtils.hasText(queryUserId)) {
			return queryUserId.trim();
		}
		String headerUserId = request.getHeaders().getFirst("X-User-Id");
		if (!StringUtils.hasText(headerUserId)) {
			return null;
		}
		return headerUserId.trim();
	}
}
