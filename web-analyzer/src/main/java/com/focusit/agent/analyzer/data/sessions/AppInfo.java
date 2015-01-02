package com.focusit.agent.analyzer.data.sessions;

/**
 * Created by Denis V. Kirpichenkov on 31.12.14.
 */
public class AppInfo {
	public final long id;
	public final long name;

	public AppInfo(long id, long name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AppInfo appInfo = (AppInfo) o;

		if (id != appInfo.id) return false;
		if (name != appInfo.name) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (int) (name ^ (name >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "AppInfo{" +
			"id=" + id +
			", name=" + name +
			'}';
	}
}
