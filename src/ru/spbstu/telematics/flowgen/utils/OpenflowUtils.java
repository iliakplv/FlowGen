package ru.spbstu.telematics.flowgen.utils;


public class OpenflowUtils {

	// Ports
	public static final int MIN_PORT = 1;
	public static final int MAX_PORT = 255;

	// Rule components labels
	public static final String RULE_DPID = 				"switch";
	public static final String RULE_FLOW_NAME = 		"name";
	public static final String RULE_PRIORITY = 			"priority";
	public static final String RULE_ACTIVE = 			"active";
	public static final String RULE_IN_PORT = 			"ingress_port";
	public static final String RULE_ACTIONS = 			"actions";
	public static final String RULE_OUT_PORTS_PREFIX =	"output=";


	// Rule priority
	public static final int DEFAULT_RULE_PRIORITY = 32767;
	public static final int MAX_RULE_PRIORITY = 32767;
	public static final int MIN_RULE_PRIORITY = 0;

	// Other
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
		return priority >= MIN_RULE_PRIORITY && priority <= MAX_RULE_PRIORITY;

	}

}
