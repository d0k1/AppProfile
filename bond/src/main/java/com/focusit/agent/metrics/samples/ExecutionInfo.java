package com.focusit.agent.metrics.samples;

import java.nio.ByteBuffer;

/**
 * Minimal profiling sample. Store method execution data in executing thread context
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public final class ExecutionInfo implements Sample<ExecutionInfo> {
	/**
	 * Thread Id
	 */
	public long threadId = -1;

	/**
	 * event eventId
	 */
	public long eventId = -1;

	/**
	 * nanos at exit
	 */
	public long time = -1;

	/**
	 * method map index
	 */
	public long method = -1;

	public long timestamp;

	public static int sizeOf(){
		return 5 * 8; // 4 field, each field - 8 bytes
	}

	@Override
	public Sample<ExecutionInfo> copyDataFrom(Sample<ExecutionInfo> sample) {

		this.threadId = ((ExecutionInfo) sample).threadId;
		this.eventId = ((ExecutionInfo) sample).eventId;
		this.time = ((ExecutionInfo) sample).time;
		this.method = ((ExecutionInfo) sample).method;
		this.timestamp = ((ExecutionInfo) sample).timestamp;
		return this;
	}

	public void writeToBuffer(ByteBuffer out){
		out.putLong(threadId);
		out.putLong(eventId);
		out.putLong(time);
		out.putLong(method);
		out.putLong(timestamp);
	}

	public void readFromBuffer(ByteBuffer in){
		threadId = in.getLong();
		eventId = in.getLong();
		time = in.getLong();
		method = in.getLong();
		timestamp = in.getLong();
	}

	@Override
	public void readFromBuffer(long[] buffer) {
		threadId = buffer[0];
		eventId = buffer[1];
		time = buffer[2];
		method = buffer[3];
		timestamp = buffer[4];
	}

	@Override
	public int sizeOfSample() {
		return ExecutionInfo.sizeOf();
	}

	@Override
	public String toString() {
		return "ExecutionInfo{" +
			"threadId=" + threadId +
			", eventId=" + eventId +
			", time=" + time +
			", method=" + method +
			", timestamp=" + timestamp +
			'}';
	}
}
