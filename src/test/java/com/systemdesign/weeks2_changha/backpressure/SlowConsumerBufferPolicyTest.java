package com.systemdesign.weeks2_changha.backpressure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlowConsumerBufferPolicyTest {

	@Test
	void dropOldestKeepsMostRecentEventsWhenConsumerStalls() {
		SlowConsumerBuffer<LocationEnvelope> buffer = new SlowConsumerBuffer<>(3, BackpressurePolicy.DROP_OLDEST);

		for (long sequence = 1; sequence <= 10; sequence++) {
			buffer.offer(LocationEnvelope.withSequence(sequence));
		}

		List<Long> drainedSequences = drainSequences(buffer);
		BackpressureSnapshot snapshot = buffer.snapshot();

		assertThat(drainedSequences).containsExactly(8L, 9L, 10L);
		assertThat(snapshot.droppedOldestCount()).isEqualTo(7);
		assertThat(snapshot.droppedNewestCount()).isZero();
		assertThat(snapshot.disconnectCount()).isZero();
		assertThat(snapshot.disconnected()).isFalse();
	}

	@Test
	void dropNewestPreservesOldestBufferedEventsWhenConsumerStalls() {
		SlowConsumerBuffer<LocationEnvelope> buffer = new SlowConsumerBuffer<>(3, BackpressurePolicy.DROP_NEWEST);

		for (long sequence = 1; sequence <= 10; sequence++) {
			buffer.offer(LocationEnvelope.withSequence(sequence));
		}

		List<Long> drainedSequences = drainSequences(buffer);
		BackpressureSnapshot snapshot = buffer.snapshot();

		assertThat(drainedSequences).containsExactly(1L, 2L, 3L);
		assertThat(snapshot.droppedOldestCount()).isZero();
		assertThat(snapshot.droppedNewestCount()).isEqualTo(7);
		assertThat(snapshot.disconnectCount()).isZero();
		assertThat(snapshot.disconnected()).isFalse();
	}

	@Test
	void disconnectStopsSessionWhenBufferIsFull() {
		SlowConsumerBuffer<LocationEnvelope> buffer = new SlowConsumerBuffer<>(3, BackpressurePolicy.DISCONNECT);

		OfferOutcome fourthOffer = OfferOutcome.ENQUEUED;
		for (long sequence = 1; sequence <= 10; sequence++) {
			OfferOutcome outcome = buffer.offer(LocationEnvelope.withSequence(sequence));
			if (sequence == 4) {
				fourthOffer = outcome;
			}
		}

		List<Long> drainedSequences = drainSequences(buffer);
		BackpressureSnapshot snapshot = buffer.snapshot();

		assertThat(fourthOffer).isEqualTo(OfferOutcome.DISCONNECTED);
		assertThat(drainedSequences).containsExactly(1L, 2L, 3L);
		assertThat(snapshot.disconnectCount()).isEqualTo(1);
		assertThat(snapshot.disconnected()).isTrue();
	}

	private List<Long> drainSequences(SlowConsumerBuffer<LocationEnvelope> buffer) {
		List<Long> drained = new ArrayList<>();
		buffer.poll().ifPresent(event -> drained.add(event.sequence()));
		while (true) {
			var item = buffer.poll();
			if (item.isEmpty()) {
				return drained;
			}
			drained.add(item.get().sequence());
		}
	}
}
