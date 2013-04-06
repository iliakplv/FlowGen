package ru.spbstu.telematics.flowgen.utils;


public class OpenflowUtils {

	// Rule priority
	public static final int MAX_FLOW_PRIORITY = 			32767;
	public static final int MIN_FLOW_PRIORITY = 			0;
	public static final int FLOW_PRIORITY_LEVEL = 			128;

	public static final int IN_TRUNK_FLOW_PRIORITY =		MAX_FLOW_PRIORITY;
	public static final int IN_HOST_FLOW_PRIORITY =			MAX_FLOW_PRIORITY - FLOW_PRIORITY_LEVEL;

	public static final int OUT_TRUNK_FLOW_PRIORITY =		MAX_FLOW_PRIORITY;
	public static final int OUT_HOST_FLOW_PRIORITY =			MAX_FLOW_PRIORITY - FLOW_PRIORITY_LEVEL;
	public static final int OUT_BROADCAST_FLOW_PRIORITY =	MAX_FLOW_PRIORITY;
	public static final int OUT_SUBNET_FLOW_PRIORITY =		MIN_FLOW_PRIORITY + FLOW_PRIORITY_LEVEL;

	// Ports
	public static final int DEFAULT_PORT = 0;
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
			return false;
		}

		if (bytesNumber < 1) {
			throw new IllegalArgumentException("Bytes number not positive");
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

	public static String setMacUniqueness(String mac, boolean globallyUnique) {
		if (!validateMac(mac)) {
			throw new IllegalArgumentException("Wrong MAC: " + mac);
		}
		mac = mac.toLowerCase();

		final int GLOBAL_UNIQUE_MASK_OR = 0x04;	// 0000 0100
		final int LOCAL_UNIQUE_MASK_AND = 0xFB;	// 1111 1011

		int firstByte = Integer.parseInt(mac.substring(0, 2), 16);
		String otherBytes = mac.substring(2);

		if (globallyUnique) {
			firstByte |= GLOBAL_UNIQUE_MASK_OR;
		} else {
			firstByte &= LOCAL_UNIQUE_MASK_AND;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toHexString(firstByte));
		sb.append(otherBytes);
		return sb.toString();
	}

	public static boolean macEquals(String mac1, String mac2) {
		mac1 = setMacUniqueness(mac1, true);
		mac2 = setMacUniqueness(mac2, true);
		return mac1.equalsIgnoreCase(mac2);
	}

	public static boolean validatePortNumber(int port) {
		return port >= MIN_PORT && port <= MAX_PORT;

	}

	public static boolean validatePriority(int priority) {
		return priority >= MIN_FLOW_PRIORITY && priority <= MAX_FLOW_PRIORITY;
	}

	public static boolean validateDatapathName(String name) {
		if (StringUtils.isNullOrEmpty(name)) {
			return false;
		}
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (!(Character.isLetterOrDigit(c) || c == '-' || c == '_')) {
				return false;
			}
		}
		return true;

	}

}
