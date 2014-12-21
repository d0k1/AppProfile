package com.focusit.agent.metrics.samples;

import java.nio.ByteBuffer;

/**
 * Hold profiling session eventId eventId
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public final class SessionInfo implements Sample<SessionInfo> {
	public long nanos;
	public long millis;
	public long time;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SessionInfo that = (SessionInfo) o;

		if (millis != that.millis) return false;
		if (nanos != that.nanos) return false;
		if (time != that.time) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (nanos ^ (nanos >>> 32));
		result = 31 * result + (int) (millis ^ (millis >>> 32));
		result = 31 * result + (int) (time ^ (time >>> 32));
		return result;
	}

	@Override
	public Sample<SessionInfo> copyDataFrom(Sample<SessionInfo> sample) {
		return sample;
	}

	@Override
	public void writeToBuffer(ByteBuffer buffer) {

	}

	@Override
	public void readFromBuffer(ByteBuffer buffer) {

	}

	@Override
	public void readFromBuffer(long[] buffer) {

	}

	@Override
	public int sizeOfSample() {
		return 8 * 3;
	}
}
