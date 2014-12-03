package com.focusit.agent.bond.metrics;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.LongBuffer;

/**
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class ExecutionInfo implements Externalizable {
	public long threadId=-1;
	public long start=-1;
	public long end=-1;
	public long method=-1;

	public final static int sizeOf(){
		return 4 * 8; // 4 field, each field - 8 bytes
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(threadId);
		out.writeLong(start);
		out.writeLong(end);
		out.writeLong(method);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		threadId = in.readLong();
		start = in.readLong();
		end = in.readLong();
		method = in.readLong();
	}

	public void writeToLongBuffer(LongBuffer out){
		out.put(threadId);
		out.put(start);
		out.put(end);
		out.put(method);
	}

	public void readFromLongBuffer(LongBuffer in){
		threadId = in.get();
		start = in.get();
		end = in.get();
		method = in.get();
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
