package ru.spbstu.telematics.flowgen.application.configuration;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

public class FloodlightConfig {

	public static final String HOST_KEY = "host";
	public static final String PORT_KEY = "port";
	public static final String PUSH_FLOWS_KEY = "push_flows";

	private String host;
	private int port;
	private boolean pushFlows;


	public FloodlightConfig(String host, int port, boolean pushFlows) {
		this.host = host;
		this.port = port;
		this.pushFlows = pushFlows;
	}


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isPushingFlows() {
		return pushFlows;
	}

	public void setPushingFlows(boolean pushFlows) {
		this.pushFlows = pushFlows;
	}

	public JSONObject export() {
		JSONObject result = new JSONObject();

		try {
			result.accumulate(HOST_KEY, host);
			result.accumulate(PORT_KEY, port);
			result.accumulate(PUSH_FLOWS_KEY, pushFlows);
		} catch (JSONException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	public static FloodlightConfig parse(JSONObject data) {
		FloodlightConfig result = null;

		try {
			String host = (String) data.get(HOST_KEY);
			int port = (Integer) data.get(PORT_KEY);
			boolean pushFlows = (Boolean) data.get(PUSH_FLOWS_KEY);

			if (!StringUtils.isNullOrEmpty(host) && port >= 0 && port <= 65535) {
				result = new FloodlightConfig(host, port, pushFlows);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}

}
