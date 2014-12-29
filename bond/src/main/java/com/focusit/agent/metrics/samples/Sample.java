package com.focusit.agent.metrics.samples;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Common interface of measuring sample
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public interface Sample<T> {

	Sample<T> copyDataFrom(Sample<T> sample);

	void writeToBuffer(ByteBuffer buffer);

	void readFromBuffer(ByteBuffer buffer);

	void writeToBuffer(ByteBuf buffer);

	void readFromBuffer(ByteBuf buffer);

	void readFromBuffer(long[] buffer);

	int sizeOfSample();

	long getAppId();
}
