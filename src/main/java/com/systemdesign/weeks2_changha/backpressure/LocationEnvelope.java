package com.systemdesign.weeks2_changha.backpressure;

public record LocationEnvelope(long sequence, long createdAtMillis) {

	public static LocationEnvelope withSequence(long sequence) {
		return new LocationEnvelope(sequence, System.currentTimeMillis());
	}
}
