package com.systemdesign.weeks2_changha.websocket;

import com.systemdesign.weeks2_changha.backpressure.BackpressurePolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.backpressure")
public class BackpressureProperties {

	private BackpressurePolicy policy = BackpressurePolicy.DROP_OLDEST;
	private int queueCapacity = 100;
	private long drainIntervalMillis = 10;
	private long simulatedSendDelayMillis = 0;

	public BackpressurePolicy getPolicy() {
		return policy;
	}

	public void setPolicy(BackpressurePolicy policy) {
		this.policy = policy;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public long getDrainIntervalMillis() {
		return drainIntervalMillis;
	}

	public void setDrainIntervalMillis(long drainIntervalMillis) {
		this.drainIntervalMillis = drainIntervalMillis;
	}

	public long getSimulatedSendDelayMillis() {
		return simulatedSendDelayMillis;
	}

	public void setSimulatedSendDelayMillis(long simulatedSendDelayMillis) {
		this.simulatedSendDelayMillis = simulatedSendDelayMillis;
	}
}
