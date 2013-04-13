package ru.spbstu.telematics.flowgen.application.configuration;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CloudConfig {

	public static final String NAME_KEY = "name";
	public static final String SERVERS_KEY = "servers";
	public static final String DATAPATHS_KEY = "datapaths";
	public static final String FLOODLIGHT_KEY = "floodlight";

	private String name;
	private List<ServerConfig> servers;
	private List<DatapathConfig> datapaths;
	private FloodlightConfig floodlight;


	public CloudConfig(String name, FloodlightConfig floodlight) {
		this.name = name;
		this.floodlight = floodlight;
		servers = new ArrayList<ServerConfig>();
		datapaths = new ArrayList<DatapathConfig>();
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FloodlightConfig getFloodlight() {
		return floodlight;
	}

	public void setFloodlight(FloodlightConfig floodlight) {
		this.floodlight = floodlight;
	}

	public void addServer(ServerConfig serverConfig) {
		if (serverConfig != null) {
			servers.add(serverConfig);
		}
	}

	public void addDatapath(DatapathConfig datapathConfig) {
		if (datapathConfig != null) {
			datapaths.add(datapathConfig);
		}
	}

	public List<ServerConfig> getServers() {
		return new ArrayList<ServerConfig>(servers);
	}

	public List<DatapathConfig> getDatapaths() {
		return new ArrayList<DatapathConfig>(datapaths);
	}


	public JSONObject export() {
		JSONObject result = new JSONObject();

		try {
			result.accumulate(NAME_KEY, name);
			result.accumulate(FLOODLIGHT_KEY, floodlight.export());

			JSONArray servers = new JSONArray();
			for (ServerConfig serverConfig : this.servers) {
				servers.put(serverConfig.export());
			}
			result.accumulate(SERVERS_KEY, servers);

			JSONArray datapaths = new JSONArray();
			for (DatapathConfig datapathConfig : this.datapaths) {
				datapaths.put(datapathConfig.export());
			}
			result.accumulate(DATAPATHS_KEY, datapaths);

		} catch (JSONException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	public static CloudConfig parse(JSONObject data) {
		CloudConfig result;

		try {
			String name = (String) data.get(NAME_KEY);
			FloodlightConfig floodlight = FloodlightConfig.parse(data.getJSONObject(FLOODLIGHT_KEY));
			if (floodlight == null) {
				return null;
			}

			result = new CloudConfig(name, floodlight);

			JSONArray servers = data.getJSONArray(SERVERS_KEY);
			for (int i = 0; i < servers.length(); i++) {
				result.addServer(ServerConfig.parse(servers.getJSONObject(i)));
			}

			JSONArray datapaths = data.getJSONArray(DATAPATHS_KEY);
			for (int i = 0; i < datapaths.length(); i++) {
				result.addDatapath(DatapathConfig.parse(datapaths.getJSONObject(i)));
			}

		} catch (JSONException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}
}
