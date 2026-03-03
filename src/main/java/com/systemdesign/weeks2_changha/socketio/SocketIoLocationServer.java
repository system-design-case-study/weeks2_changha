package com.systemdesign.weeks2_changha.socketio;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.AuthorizationResult;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.systemdesign.weeks2_changha.friend.FriendRelationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SocketIoLocationServer {

	private static final Logger log = LoggerFactory.getLogger(SocketIoLocationServer.class);

	private static final String USER_ID_PARAM = "userId";
	private static final String USER_ID_KEY = "userId";
	private static final String LOCATION_UPDATE_EVENT = "location:update";

	private final SocketIoProperties properties;
	private final FriendRelationService friendRelationService;

	private SocketIOServer server;
	private SocketIONamespace namespace;
	private RedissonClient redisBackplaneClient;

	public SocketIoLocationServer(SocketIoProperties properties, FriendRelationService friendRelationService) {
		this.properties = properties;
		this.friendRelationService = friendRelationService;
	}

	@PostConstruct
	public void start() {
		if (!properties.isEnabled()) {
			log.info("Socket.IO server is disabled");
			return;
		}

		Configuration configuration = new Configuration();
		configuration.setHostname(properties.getHost());
		configuration.setPort(properties.getPort());
		configuration.setOrigin(properties.getOrigin());
		configuration.setAuthorizationListener(data -> isAuthorized(data)
			? AuthorizationResult.SUCCESSFUL_AUTHORIZATION
			: AuthorizationResult.FAILED_AUTHORIZATION);
		configureRedisBackplane(configuration);

		this.server = new SocketIOServer(configuration);
		this.namespace = server.addNamespace(normalizeNamespace(properties.getNamespace()));
		registerListeners(namespace);
		server.start();

		log.info(
			"Socket.IO server started on {}:{} namespace={}",
			properties.getHost(),
			properties.getPort(),
			namespace.getName()
		);
	}

	@PreDestroy
	public void stop() {
		if (server == null) {
			return;
		}
		server.stop();
		if (redisBackplaneClient != null) {
			redisBackplaneClient.shutdown();
		}
		log.info("Socket.IO server stopped");
	}

	private void configureRedisBackplane(Configuration configuration) {
		SocketIoProperties.Redis redis = properties.getRedis();
		if (redis == null || !redis.isEnabled()) {
			return;
		}

		Config redisConfig = new Config();
		SingleServerConfig singleServerConfig = redisConfig.useSingleServer()
			.setAddress(redis.getAddress())
			.setDatabase(redis.getDatabase());

		if (StringUtils.hasText(redis.getUsername())) {
			singleServerConfig.setUsername(redis.getUsername());
		}
		if (StringUtils.hasText(redis.getPassword())) {
			singleServerConfig.setPassword(redis.getPassword());
		}

		this.redisBackplaneClient = Redisson.create(redisConfig);
		configuration.setStoreFactory(new RedissonStoreFactory(redisBackplaneClient));
		log.info("Socket.IO Redis backplane enabled: address={}, database={}", redis.getAddress(), redis.getDatabase());
	}

	private void registerListeners(SocketIONamespace nsp) {
		nsp.addConnectListener(client -> {
			String userId = extractUserId(client.getHandshakeData());
			if (!StringUtils.hasText(userId)) {
				client.disconnect();
				return;
			}
			client.set(USER_ID_KEY, userId);
			client.joinRoom(userRoom(userId));
		});

		nsp.addEventListener(
			LOCATION_UPDATE_EVENT,
			SocketIoLocationEvent.class,
			this::handleLocationUpdate
		);
	}

	private void handleLocationUpdate(SocketIOClient client, SocketIoLocationEvent inbound, AckRequest ackRequest) {
		String senderUserId = client.get(USER_ID_KEY);
		if (!StringUtils.hasText(senderUserId)) {
			client.disconnect();
			return;
		}

		SocketIoLocationEvent outbound = new SocketIoLocationEvent();
		outbound.setUserId(senderUserId);
		outbound.setLat(inbound.getLat());
		outbound.setLon(inbound.getLon());
		outbound.setSequence(inbound.getSequence());
		outbound.setClientTs(inbound.getClientTs());
		outbound.setServerTs(System.currentTimeMillis());

		Set<String> friendUserIds = friendRelationService.findFriendIds(senderUserId);
		friendUserIds.forEach(friendUserId -> namespace
			.getRoomOperations(userRoom(friendUserId))
			.sendEvent(LOCATION_UPDATE_EVENT, outbound));

		if (ackRequest.isAckRequested()) {
			ackRequest.sendAckData(friendUserIds.size());
		}
	}

	private boolean isAuthorized(HandshakeData data) {
		return StringUtils.hasText(extractUserId(data));
	}

	private String extractUserId(HandshakeData handshakeData) {
		String userIdFromQuery = handshakeData.getSingleUrlParam(USER_ID_PARAM);
		if (StringUtils.hasText(userIdFromQuery)) {
			return userIdFromQuery.trim();
		}
		String userIdFromHeader = handshakeData.getHttpHeaders().get("X-User-Id");
		if (!StringUtils.hasText(userIdFromHeader)) {
			return null;
		}
		return userIdFromHeader.trim();
	}

	private String userRoom(String userId) {
		return "user:" + userId;
	}

	private String normalizeNamespace(String namespaceValue) {
		if (!StringUtils.hasText(namespaceValue)) {
			return "/location";
		}
		if (namespaceValue.startsWith("/")) {
			return namespaceValue;
		}
		return "/" + namespaceValue;
	}
}
