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
		this.dpid = dpid;
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


	public static DatapathData parse(JSONObject data) throws JSONException {
		String dpid = (String) data.get(DPID_KEY);
		DatapathData result = new DatapathData(dpid);

		JSONArray portsJsonArray = data.getJSONArray(PORTS_KEY);
		int numberOfPorts = portsJsonArray.length();

		for (int i = 0; i < numberOfPorts; i++) {
			JSONObject portJsonObject = (JSONObject) portsJsonArray.get(i);
			result.addPortData(PortData.parse(portJsonObject));
		}

		return result;
	}

}
