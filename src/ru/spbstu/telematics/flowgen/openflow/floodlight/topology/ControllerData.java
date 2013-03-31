package ru.spbstu.telematics.flowgen.openflow.floodlight.topology;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ControllerData {

	private Map<String, DatapathData> datapaths;


	public ControllerData() {
		datapaths = new HashMap<String, DatapathData>();
	}


	public void addDatapathData(DatapathData datapathData) {
		datapaths.put(datapathData.getDpid(), datapathData);
	}

	public DatapathData getDatapathData(String dpid) {
		return datapaths.get(dpid.toLowerCase());
	}

	public void removeDatapathData(String dpid) {
		datapaths.remove(dpid.toLowerCase());
	}

	public boolean containsDatapath(String dpid) {
		return datapaths.containsKey(dpid.toLowerCase());
	}

	public Set<String> getDpids() {
		return datapaths.keySet();
	}

	public Collection<DatapathData> getDatapaths() {
		return datapaths.values();

	}


	public static ControllerData parse(JSONArray data) throws JSONException {
		ControllerData result = new ControllerData();

		int numberOfDatapaths = data.length();

		for (int i = 0; i < numberOfDatapaths; i++) {
			JSONObject datapathJsonObject = (JSONObject) data.get(i);
			result.addDatapathData(DatapathData.parse(datapathJsonObject));
		}

		return result;
	}



}
