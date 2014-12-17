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
	 * nanos at start
	 */
	public long start = -1;

	/**
	 * nanos at exit
	 */
	public long end = -1;

	/**
	 * method map index
	 */
	public long method = -1;

	public static int sizeOf(){
		return 4 * 8; // 4 field, each field - 8 bytes
	}

	@Override
	public Sample<ExecutionInfo> copyDataFrom(Sample<ExecutionInfo> sample) {

		this.threadId = ((ExecutionInfo) sample).threadId;
		this.start = ((ExecutionInfo) sample).start;
		this.end = ((ExecutionInfo) sample).end;
		this.method = ((ExecutionInfo) sample).method;
		return this;
	}

	public void writeToBuffer(ByteBuffer out){
		out.putLong(threadId);
		out.putLong(start);
		out.putLong(end);
		out.putLong(method);
	}

	public void readFromBuffer(ByteBuffer in){
		threadId = in.getLong();
		start = in.getLong();
		end = in.getLong();
		method = in.getLong();
	}

	@Override
	public int sizeOfSample() {
		return ExecutionInfo.sizeOf();
	}

	@Override
	public String toString() {
		return "ExecutionInfo{" +
			"threadId=" + threadId +
			", start=" + start +
			", end=" + end +
			", method=" + method +
			'}';
	}
}
