package com.focusit.agent.analyzer.data.sessions;

/**
 * Created by Denis V. Kirpichenkov on 05.01.15.
 */
public class RecordInfo {
	public final long id;
	public final String name;

	public RecordInfo(long id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RecordInfo that = (RecordInfo) o;

		if (id != that.id) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "RecordInfo{" +
			"id=" + id +
			", name='" + name + '\'' +
			'}';
	}
}
