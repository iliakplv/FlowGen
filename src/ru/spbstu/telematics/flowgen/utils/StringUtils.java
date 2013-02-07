package ru.spbstu.telematics.flowgen.utils;


public class StringUtils {

	public static final String EMPTY_STRING = "";


	public static boolean isNullOrEmpty(String s) {
		return (s == null) || (s.length() == 0);
	}

	public static String getNotNull(String s) {
		return s != null ? s : EMPTY_STRING;
	}

	public static boolean isHexDigit(char c) {
		return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');

	}

	public static String buildFromParts(String[] parts, String delimiter) {
		if (parts == null || parts.length == 0) {
			throw new IllegalArgumentException("Parts array is null or empty");
		}

		delimiter = getNotNull(delimiter);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < parts.length; i++) {
			sb.append(parts[i]);
			if(i != parts.length -1) {
				sb.append(delimiter);
			}
		}

		return sb.toString();
	}

}
