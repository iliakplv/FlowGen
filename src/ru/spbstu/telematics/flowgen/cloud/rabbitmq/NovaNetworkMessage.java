package ru.spbstu.telematics.flowgen.cloud.rabbitmq;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;

public class NovaNetworkMessage {

	public static final String METHOD_KEY = "method";
	public static final String METHOD_LEASE_VALUE = "lease_fixed_ip";
	public static final String METHOD_RELEASE_VALUE = "release_fixed_ip";
	public static final String ARGS_KEY = "args";
	public static final String IP_KEY = "address";


	private MessageType type;
	private String ip;


	public NovaNetworkMessage(MessageType type, String ip) {
		if (!OpenflowUtils.validateIpv4(ip)) {
			throw new IllegalArgumentException("Wrong IP " + ip);
		}
		this.type = type;
		this.ip = ip;
	}

	public MessageType getMessageType() {
		return type;
	}

	public String getIp() {
		return ip;
	}

	public static NovaNetworkMessage parse(JSONObject data) {
		NovaNetworkMessage result;

		try {
			String method = (String) data.get(METHOD_KEY);
			JSONObject args = data.getJSONObject(ARGS_KEY);
			String ip = (String) args.get(IP_KEY);

			if (METHOD_LEASE_VALUE.equals(method)) {
				result = new NovaNetworkMessage(MessageType.Launch, ip);
			} else if (METHOD_RELEASE_VALUE.equals(method)) {
				result = new NovaNetworkMessage(MessageType.Terminate, ip);
			} else {
				result = new NovaNetworkMessage(MessageType.Other, ip);
			}

		} catch (JSONException e) {
			result = new NovaNetworkMessage(MessageType.Other, "0.0.0.0");
		}

		return result;
	}


	/***** Inner Classes *****/

	public static enum MessageType {
		Launch,
		Terminate,
		Other
	}
}
