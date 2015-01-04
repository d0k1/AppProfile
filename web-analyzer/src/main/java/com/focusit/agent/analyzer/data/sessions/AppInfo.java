package com.focusit.agent.analyzer.data.sessions;

/**
 * Created by Denis V. Kirpichenkov on 31.12.14.
 */
public class AppInfo {
	public final long id;
	public final String name;

	public AppInfo(long id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return "AppInfo{" +
			"id=" + id +
			", name='" + name + '\'' +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AppInfo appInfo = (AppInfo) o;

		if (id != appInfo.id) return false;
		if (name != null ? !name.equals(appInfo.name) : appInfo.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
