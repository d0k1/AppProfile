package com.focusit.agent.utils.common;

/**
 * Created by Denis V. Kirpichenkov on 13.12.14.
 */
public class StringUtils {
	private static String EMPTY_STRING = " ";

	public static boolean isEmpty(String string) {
		if (string == null)
			return true;

		return string.length() == 0 || string.equals(EMPTY_STRING);
	}
}
