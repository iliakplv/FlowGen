package ru.spbstu.telematics.flowgen.application.configuration;


import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfig {

	public static final String HOST_KEY = "host";
	public static final String PORT_KEY = "port";
	public static final String ACTIVE_KEY = "active";
	public static final String QUEUE_KEY = "network_queue";
	public static final String ROUTING_KEY_KEY = "network_routing_key";

	private String host;
	private int port;
	private String queueName;
	private String routingKey;
	private boolean active;


	public ServerConfig(String host, int port, String queueName, String routingKey, boolean active) {
		this.host = host;
		this.port = port;
		this.queueName = queueName;
		this.routingKey = routingKey;
		this.active = active;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}


	public JSONObject export() {
		JSONObject result = new JSONObject();

		try {
			result.accumulate(HOST_KEY, host);
			result.accumulate(PORT_KEY, port);
			result.accumulate(QUEUE_KEY, queueName);
			result.accumulate(ROUTING_KEY_KEY, routingKey);
			result.accumulate(ACTIVE_KEY, active);
		} catch (JSONException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	public static ServerConfig parse(JSONObject data) {
		ServerConfig result = null;

		try {
			String host = (String) data.get(HOST_KEY);
			int port = (Integer) data.get(PORT_KEY);
			String queue = (String) data.get(QUEUE_KEY);
			String routingKey = (String) data.get(ROUTING_KEY_KEY);
			boolean active = (Boolean) data.get(ACTIVE_KEY);

			result = new ServerConfig(host, port, queue, routingKey, active);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
}
