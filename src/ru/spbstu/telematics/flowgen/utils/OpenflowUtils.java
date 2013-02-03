package ru.spbstu.telematics.flowgen.utils;


public class OpenflowUtils {

	public static final int MIN_PORT = 1;
	public static final int MAX_PORT = 255;

	public static final int DPID_BYTES = 8;
	public static final int MAC_BYTES = 6;

	public static final char DPID_DELIMITER = ':';
	public static final char MAC_DELIMITER = ':';
	public static final char PORTS_DELIMITER = ',';


	public static boolean validateDpid(String dpid) {
        return validateHexByteString(dpid, DPID_BYTES, DPID_DELIMITER);
	}

	public static boolean validateMac(String mac) {
		return validateHexByteString(mac, MAC_BYTES, MAC_DELIMITER);
	}

	public static boolean validateHexByteString(String string, int bytesNumber, char delimiter) {
		final int HEX_BYTE_LENGTH = 2;

		if (StringUtils.isNullOrEmpty(string)) {
			throw new IllegalArgumentException("String is null");
		}

		if (bytesNumber < 1) {
			throw new IllegalArgumentException("Wrong bytes number");
		}

		if (StringUtils.isHexDigit(delimiter)) {
			throw new IllegalArgumentException("Delimiter equals to hexadecimal number");
		}

		String delimiterString = Character.valueOf(delimiter).toString();

		if (string.startsWith(delimiterString) || string.endsWith(delimiterString)) {
			return false;
		}

		String[] bytes = string.split(delimiterString);
		if (bytes.length != bytesNumber) {
			return false;
		}

		for (int i = 0; i < bytesNumber; i++) {
			if(bytes[i].length() != HEX_BYTE_LENGTH) {
				return false;
			}
			for (int j = 0; j < HEX_BYTE_LENGTH; j++) {
				if (!StringUtils.isHexDigit(bytes[i].charAt(j))) {
					return false;
				}
			}

		}

		return true;
	}

	public static boolean validatePortNumber(int port) {
		return port >= MIN_PORT && port <= MAX_PORT;

	}

}
