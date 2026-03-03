package com.systemdesign.weeks2_changha.websocket;

import com.systemdesign.weeks2_changha.backpressure.OfferOutcome;
import com.systemdesign.weeks2_changha.backpressure.SlowConsumerBuffer;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

class OutboundSessionDispatcher {

	private static final Logger log = LoggerFactory.getLogger(OutboundSessionDispatcher.class);

	private final WebSocketSession session;
	private final SlowConsumerBuffer<String> buffer;
	private final long drainIntervalMillis;
	private final long simulatedSendDelayMillis;
	private final AtomicBoolean running = new AtomicBoolean(true);
	private final Thread worker;

	OutboundSessionDispatcher(WebSocketSession session, BackpressureProperties properties) {
		this.session = session;
		this.buffer = new SlowConsumerBuffer<>(properties.getQueueCapacity(), properties.getPolicy());
		this.drainIntervalMillis = properties.getDrainIntervalMillis();
		this.simulatedSendDelayMillis = resolveSendDelayMillis(session, properties);
		this.worker = Thread.ofVirtual()
			.name("ws-outbound-" + session.getId())
			.start(this::drainLoop);
	}

	OfferOutcome enqueue(String payload) {
		OfferOutcome outcome = buffer.offer(payload);
		if (outcome == OfferOutcome.DISCONNECTED) {
			closeSlowConsumerSession();
		}
		return outcome;
	}

	void shutdown() {
		if (!running.getAndSet(false)) {
			return;
		}
		worker.interrupt();
	}

	private void drainLoop() {
		while (running.get() && session.isOpen()) {
			Optional<String> maybePayload = buffer.poll();
			if (maybePayload.isEmpty()) {
				sleepQuietly(drainIntervalMillis);
				continue;
			}
			sendWithOptionalDelay(maybePayload.get());
		}
	}

	private void sendWithOptionalDelay(String payload) {
		if (simulatedSendDelayMillis > 0) {
			sleepQuietly(simulatedSendDelayMillis);
		}
		if (!session.isOpen()) {
			return;
		}
		try {
			session.sendMessage(new TextMessage(payload));
		} catch (IOException ex) {
			log.warn("WebSocket send failed for session {}", session.getId(), ex);
			closeSession(CloseStatus.SERVER_ERROR);
		}
	}

	private void closeSlowConsumerSession() {
		log.info("Closing slow consumer session {}", session.getId());
		closeSession(CloseStatus.POLICY_VIOLATION);
	}

	private void closeSession(CloseStatus closeStatus) {
		try {
			if (session.isOpen()) {
				session.close(closeStatus);
			}
		} catch (IOException ex) {
			log.debug("Failed to close session {}", session.getId(), ex);
		} finally {
			shutdown();
		}
	}

	private void sleepQuietly(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
		}
	}

	private long resolveSendDelayMillis(WebSocketSession session, BackpressureProperties properties) {
		if (session.getUri() == null) {
			return properties.getSimulatedSendDelayMillis();
		}
		String sendDelayParam = UriComponentsBuilder.fromUri(session.getUri())
			.build()
			.getQueryParams()
			.getFirst("sendDelayMs");
		if (sendDelayParam == null || sendDelayParam.isBlank()) {
			return properties.getSimulatedSendDelayMillis();
		}
		try {
			long parsedDelay = Long.parseLong(sendDelayParam);
			return Math.max(parsedDelay, 0);
		} catch (NumberFormatException ex) {
			log.warn("Invalid sendDelayMs value [{}] for session {}", sendDelayParam, session.getId());
			return properties.getSimulatedSendDelayMillis();
		}
	}
}
