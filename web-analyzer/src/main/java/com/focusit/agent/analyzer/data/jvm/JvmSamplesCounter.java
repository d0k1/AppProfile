package com.focusit.agent.analyzer.data.jvm;

/**
 * Created by Denis V. Kirpichenkov on 14.01.15.
 */
public class JvmSamplesCounter {
	public final long appId;
	public final long sessionId;
	public final long recId;
	public final long samples;
	public final long minTimestamp;
	public final long maxTimestamp;

	public JvmSamplesCounter(long appId, long sessionId, long recId, long samples, long minTimestamp, long maxTimestamp) {
		this.appId = appId;
		this.sessionId = sessionId;
		this.recId = recId;
		this.samples = samples;
		this.minTimestamp = minTimestamp;
		this.maxTimestamp = maxTimestamp;
	}
}
