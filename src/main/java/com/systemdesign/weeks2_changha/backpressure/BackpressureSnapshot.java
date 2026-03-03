package com.systemdesign.weeks2_changha.backpressure;

public record BackpressureSnapshot(
	int capacity,
	int queueDepth,
	long enqueuedCount,
	long droppedOldestCount,
	long droppedNewestCount,
	long disconnectCount,
	boolean disconnected
) {
}
