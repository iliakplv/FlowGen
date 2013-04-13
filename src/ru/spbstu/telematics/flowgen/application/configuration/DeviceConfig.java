package ru.spbstu.telematics.flowgen.application.configuration;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;

public class DeviceConfig {

	public static final String MAC_KEY = "mac";
	public static final String PORT_KEY = "port";
	public static final String ACTIVE_KEY = "active";

	private String mac;
	private int port;
	private boolean active;


	public DeviceConfig(String mac, int port, boolean active) {
		this.mac = mac;
		this.port = port;
		this.active = active;
	}


	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public JSONObject export() {
		JSONObject result = new JSONObject();

		try {
			result.accumulate(MAC_KEY, mac);
			result.accumulate(PORT_KEY, port);
			result.accumulate(ACTIVE_KEY, active);
		} catch (JSONException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	public static DeviceConfig parse(JSONObject data) {
		DeviceConfig result = null;

		try {
			String mac = (String) data.get(MAC_KEY);
			int port = (Integer) data.get(PORT_KEY);
			boolean active = Boolean.parseBoolean((String) data.get(ACTIVE_KEY));

			if (OpenflowUtils.validateMac(mac) && OpenflowUtils.validatePortNumber(port)) {
				result = new DeviceConfig(mac, port, active);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
}
