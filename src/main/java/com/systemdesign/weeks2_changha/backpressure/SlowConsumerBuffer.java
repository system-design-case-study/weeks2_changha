package com.systemdesign.weeks2_changha.backpressure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public class SlowConsumerBuffer<T> {

	private final int capacity;
	private final BackpressurePolicy policy;
	private final Deque<T> queue;

	private long enqueuedCount;
	private long droppedOldestCount;
	private long droppedNewestCount;
	private long disconnectCount;
	private boolean disconnected;

	public SlowConsumerBuffer(int capacity, BackpressurePolicy policy) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity must be greater than zero");
		}
		this.capacity = capacity;
		this.policy = Objects.requireNonNull(policy, "policy");
		this.queue = new ArrayDeque<>(capacity);
	}

	public synchronized OfferOutcome offer(T item) {
		Objects.requireNonNull(item, "item");

		if (disconnected) {
			return OfferOutcome.DISCONNECTED;
		}

		if (queue.size() < capacity) {
			queue.addLast(item);
			enqueuedCount++;
			return OfferOutcome.ENQUEUED;
		}

		return switch (policy) {
			case DROP_OLDEST -> {
				queue.removeFirst();
				droppedOldestCount++;
				queue.addLast(item);
				enqueuedCount++;
				yield OfferOutcome.DROPPED_OLDEST;
			}
			case DROP_NEWEST -> {
				droppedNewestCount++;
				yield OfferOutcome.DROPPED_NEWEST;
			}
			case DISCONNECT -> {
				disconnected = true;
				disconnectCount++;
				yield OfferOutcome.DISCONNECTED;
			}
		};
	}

	public synchronized Optional<T> poll() {
		if (queue.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(queue.removeFirst());
	}

	public synchronized BackpressureSnapshot snapshot() {
		return new BackpressureSnapshot(
			capacity,
			queue.size(),
			enqueuedCount,
			droppedOldestCount,
			droppedNewestCount,
			disconnectCount,
			disconnected
		);
	}
}
