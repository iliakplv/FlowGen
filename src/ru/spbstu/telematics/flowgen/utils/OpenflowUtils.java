package ru.spbstu.telematics.flowgen.utils;


public class OpenflowUtils {

	// Rule priority
	public static final int MAX_FLOW_PRIORITY = 			32768; // 0x8000
	public static final int MIN_FLOW_PRIORITY = 			0;
	public static final int IN_FLOW_PRIORITY =				MAX_FLOW_PRIORITY;
	public static final int OUT_GATEWAY_FLOW_PRIORITY =		MAX_FLOW_PRIORITY;
	public static final int OUT_VM_FLOW_PRIORITY =			MAX_FLOW_PRIORITY / 2;
	public static final int OUT_SUBNET_FLOW_PRIORITY =		MAX_FLOW_PRIORITY / 4;

	// Ports
	public static final int MIN_PORT = 1;
	public static final int MAX_PORT = 255;

	// Other
	public static final int DPID_BYTES = 8;
	public static final int MAC_BYTES = 6;

	public static final char DPID_DELIMITER = ':';
	public static final char MAC_DELIMITER = ':';


	public static boolean validateDpid(String dpid) {
    	return validateHexByteString(dpid, DPID_BYTES, DPID_DELIMITER);
	}

	public static boolean validateMac(String mac) {
		return validateHexByteString(mac, MAC_BYTES, MAC_DELIMITER);
	}

	public static boolean validateHexByteString(String string, int bytesNumber, char delimiter) {
		final int HEX_BYTE_LENGTH = 2;

		if (StringUtils.isNullOrEmpty(string)) {
			throw new IllegalArgumentException("String is null or empty");
		}

		if (bytesNumber < 1) {
			throw new IllegalArgumentException("Wrong bytes number");
		}

		if (StringUtils.isHexDigit(delimiter)) {
			throw new IllegalArgumentException("No difference between delimiter and hexadecimal digit");
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

	public static boolean validatePriority(int priority) {
		return priority >= MIN_FLOW_PRIORITY && priority <= MAX_FLOW_PRIORITY;

	}

}
