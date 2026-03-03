package com.systemdesign.weeks2_changha.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class WsUserHandshakeInterceptorTest {

	private final WsUserHandshakeInterceptor interceptor = new WsUserHandshakeInterceptor();

	@Test
	void acceptsConnectionWhenUserIdQueryParamExists() {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/ws/location");
		servletRequest.setQueryString("userId=user-a");
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		Map<String, Object> attributes = new HashMap<>();

		boolean accepted = interceptor.beforeHandshake(
			new ServletServerHttpRequest(servletRequest),
			new ServletServerHttpResponse(servletResponse),
			null,
			attributes
		);

		assertThat(accepted).isTrue();
		assertThat(attributes).containsEntry(WsSessionAttributes.USER_ID, "user-a");
	}

	@Test
	void rejectsConnectionWhenNoUserIdentityProvided() {
		MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/ws/location");
		MockHttpServletResponse servletResponse = new MockHttpServletResponse();
		Map<String, Object> attributes = new HashMap<>();

		boolean accepted = interceptor.beforeHandshake(
			new ServletServerHttpRequest(servletRequest),
			new ServletServerHttpResponse(servletResponse),
			null,
			attributes
		);

		assertThat(accepted).isFalse();
		assertThat(servletResponse.getStatus()).isEqualTo(401);
		assertThat(attributes).doesNotContainKey(WsSessionAttributes.USER_ID);
	}
}
