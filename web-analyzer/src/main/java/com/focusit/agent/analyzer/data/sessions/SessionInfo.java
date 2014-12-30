package com.focusit.agent.analyzer.data.sessions;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
public class SessionInfo {
	public final long sessionId;
	public final long jvmSamples;
	public final long methods;
	public final long statisticsSamples;
	public final long date;

	public SessionInfo(long sessionId, long jvmSamples, long statisticsSamples, long methods, long date) {
		this.sessionId = sessionId;
		this.jvmSamples = jvmSamples;
		this.statisticsSamples = statisticsSamples;
		this.methods = methods;
		this.date = date;
	}
}
