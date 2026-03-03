package com.systemdesign.weeks2_changha.backpressure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlowConsumerPolicyComparisonTest {

	@Test
	void dropOldestMaintainsFreshnessBetterThanDropNewestForSlowConsumers() {
		SimulationResult dropOldest = runSimulation(BackpressurePolicy.DROP_OLDEST, 3, 30, 6);
		SimulationResult dropNewest = runSimulation(BackpressurePolicy.DROP_NEWEST, 3, 30, 6);
		SimulationResult disconnect = runSimulation(BackpressurePolicy.DISCONNECT, 3, 30, 6);

		assertThat(dropOldest.latestDeliveredSequence()).isEqualTo(30);
		assertThat(dropNewest.latestDeliveredSequence()).isLessThan(30);
		assertThat(dropOldest.snapshot().droppedOldestCount()).isGreaterThan(0);
		assertThat(dropNewest.snapshot().droppedNewestCount()).isGreaterThan(0);
		assertThat(disconnect.snapshot().disconnected()).isTrue();
	}

	private SimulationResult runSimulation(
		BackpressurePolicy policy,
		int capacity,
		long producedCount,
		int consumeEvery
	) {
		SlowConsumerBuffer<LocationEnvelope> buffer = new SlowConsumerBuffer<>(capacity, policy);
		List<Long> delivered = new ArrayList<>();

		for (long sequence = 1; sequence <= producedCount; sequence++) {
			buffer.offer(LocationEnvelope.withSequence(sequence));
			if (sequence % consumeEvery == 0) {
				buffer.poll().ifPresent(event -> delivered.add(event.sequence()));
			}
		}

		while (true) {
			var event = buffer.poll();
			if (event.isEmpty()) {
				break;
			}
			delivered.add(event.get().sequence());
		}

		long latestDelivered = delivered.isEmpty() ? -1 : delivered.get(delivered.size() - 1);
		return new SimulationResult(latestDelivered, buffer.snapshot());
	}

	private record SimulationResult(long latestDeliveredSequence, BackpressureSnapshot snapshot) {
	}
}
