package ru.spbstu.telematics.flowgen.openflow.floodlight.topology;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DatapathData {

	public static final String DPID_KEY = "dpid";
	public static final String PORTS_KEY = "ports";

	private String dpid;
	private Map<Integer, PortData> ports;


	public DatapathData(String dpid) {
		this.dpid = dpid.toLowerCase();
		ports = new HashMap<Integer, PortData>();
	}


	public String getDpid() {
		return dpid;
	}

	public void addPortData(PortData portData) {
		ports.put(portData.getNumber(), portData);
	}

	public PortData getPortData(int portNumber) {
		return ports.get(portNumber);
	}

	public void removePortData(int portNumber) {
		ports.remove(portNumber);
	}

	public boolean containsPort(int portNumber) {
		return ports.containsKey(portNumber);
	}

	public Set<Integer> getPortNumbers() {
		return ports.keySet();
	}

	public Collection<PortData> getPorts() {
		return ports.values();
	}


	public static DatapathData parse(JSONObject data) {

		String dpid;
		DatapathData result;
		JSONArray portsJsonArray;

		try {
			dpid = (String) data.get(DPID_KEY);
			portsJsonArray = data.getJSONArray(PORTS_KEY);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		result = new DatapathData(dpid);
        int numberOfPorts = portsJsonArray.length();

		for (int i = 0; i < numberOfPorts; i++) {
			JSONObject portJsonObject;
			try {
				portJsonObject = (JSONObject) portsJsonArray.get(i);
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}

			PortData portData = PortData.parse(portJsonObject);
			if (portData != null) {
				result.addPortData(portData);
			}
		}

		return result;
	}

}
