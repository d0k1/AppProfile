package com.focusit.agent.metrics.samples;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Minimal profiling sample. Store method execution data in executing thread context
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public final class ExecutionInfo implements Sample<ExecutionInfo> {
	/**
	 * Thread Id
	 */
	public int threadId = -1;

	/**
	 * event eventId
	 */
	public byte eventId = -1;

	/**
	 * nanos at exit
	 */
	public long time = -1;

	/**
	 * method map index
	 */
	public long method = -1;

	public long appId;

	public static int sizeOf(){
		return 3 * 8 + 1 + 4; // 3 longs + 1 byte + 1 int = 29 bytes
	}

	@Override
	public Sample<ExecutionInfo> copyDataFrom(Sample<ExecutionInfo> sample) {

		this.threadId = ((ExecutionInfo) sample).threadId;
		this.eventId = ((ExecutionInfo) sample).eventId;
		this.time = ((ExecutionInfo) sample).time;
		this.method = ((ExecutionInfo) sample).method;
		this.appId = ((ExecutionInfo) sample).appId;
		return this;
	}

	public void writeToBuffer(ByteBuffer out){
		out.putLong(threadId);
		out.putLong(eventId);
		out.putLong(time);
		out.putLong(method);
		out.putLong(appId);
	}

	public void readFromBuffer(ByteBuffer in){
		threadId = in.getInt();
		eventId = in.get();
		time = in.getLong();
		method = in.getLong();
		appId = in.getLong();
	}

	@Override
	public void writeToBuffer(ByteBuf out) {
		out.writeInt(threadId);
		out.writeByte(eventId);
		out.writeLong(time);
		out.writeLong(method);
		out.writeLong(appId);
	}

	@Override
	public void readFromBuffer(ByteBuf in) {
		threadId = in.readInt();
		eventId = in.readByte();
		time = in.readLong();
		method = in.readLong();
		appId = in.readLong();
	}

	@Override
	public void readFromBuffer(long[] buffer) {
		threadId = (int) buffer[0];
		eventId = (byte) buffer[1];
		time = buffer[2];
		method = buffer[3];
		appId = buffer[4];
	}

	@Override
	public int sizeOfSample() {
		return ExecutionInfo.sizeOf();
	}

	@Override
	public long getAppId() {
		return appId;
	}

	@Override
	public String toString() {
		return "ExecutionInfo{" +
			"threadId=" + threadId +
			", eventId=" + eventId +
			", time=" + time +
			", method=" + method +
			", appId=" + appId +
			'}';
	}
}
