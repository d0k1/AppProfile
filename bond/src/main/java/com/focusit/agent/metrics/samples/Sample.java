package com.focusit.agent.metrics.samples;

import java.nio.LongBuffer;

/**
 * Common interface of measuring sample
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public interface Sample<T> {

	Sample<T> copyDataFrom(Sample<T> sample);

	void writeToLongBuffer(LongBuffer buffer);

	void readFromLongBuffer(LongBuffer buffer);

	int sizeOfSample();
}
