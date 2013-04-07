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
		setType(type);
		setIp(ip);
	}

	public MessageType getMessageType() {
		return type;
	}

	public String getIp() {
		return ip;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public void setIp(String ip) {
		if (!OpenflowUtils.validateIpv4(ip)) {
			throw new IllegalArgumentException("Wrong IP " + ip);
		}
		this.ip = ip;
	}

	public static NovaNetworkMessage parse(JSONObject data) {

		NovaNetworkMessage result = null;

		try {
			String method = (String) data.get(METHOD_KEY);
			JSONObject args = data.getJSONObject(ARGS_KEY);
			String ip = (String) args.get(IP_KEY);

			// Message parsed. Check message method type.
			if (METHOD_LEASE_VALUE.equals(method)) {
				result = new NovaNetworkMessage(MessageType.Launch, ip);
			} else if (METHOD_RELEASE_VALUE.equals(method)) {
				result = new NovaNetworkMessage(MessageType.Terminate, ip);
			} else {
				System.out.println("[INFO] Unknown method type of Nova network message: " +
						method + " (ignored)");
			}

		} catch (JSONException e) {
			System.out.println("[INFO] Not \'VM launching or termination\' Nova network message" +
					" (parsing failed) (ignored) stacktrace:");
			e.printStackTrace();
		}

		return result;
	}


	/***** Inner Classes *****/

	public static enum MessageType {
		Launch,
		Terminate,
	}
}
