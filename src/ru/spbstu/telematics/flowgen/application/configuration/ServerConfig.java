package ru.spbstu.telematics.flowgen.application.configuration;


import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfig {

	public static final String HOST_KEY = "host";
	public static final String QUEUE_KEY = "network_queue";
	public static final String ROUTING_KEY_KEY = "network_routing_key";

	private String host;
	private String queueName;
	private String routingKey;


	public ServerConfig(String host, String queueName, String routingKey) {
		this.host = host;
		this.queueName = queueName;
		this.routingKey = routingKey;
	}


	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
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
			result.accumulate(QUEUE_KEY, queueName);
			result.accumulate(ROUTING_KEY_KEY, routingKey);
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
			String queue = (String) data.get(QUEUE_KEY);
			String routingKey = (String) data.get(ROUTING_KEY_KEY);

			result = new ServerConfig(host, queue, routingKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
}
