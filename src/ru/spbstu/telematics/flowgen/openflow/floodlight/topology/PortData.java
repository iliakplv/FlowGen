package ru.spbstu.telematics.flowgen.openflow.floodlight.topology;


import org.json.JSONException;
import org.json.JSONObject;

public class PortData {

	public static final String NAME_KEY = "name";
	public static final String NUMBER_KEY = "portNumber";
	public static final String MAC_KEY = "hardwareAddress";

	private static final int MIN_DATAPATH_RESERVED_PORT = 65534;

	private String name;
	private int number;
	private String mac;


	public PortData(String name, int number, String mac) {
		this.name = name;
		this.number = number;
		this.mac = mac;
	}


	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	public String getMac() {
		return mac;
	}

	public boolean isDatapathReservedPort() {
		return number >= MIN_DATAPATH_RESERVED_PORT;
	}


	public static PortData parse(JSONObject data) throws JSONException {
		String name = (String) data.get(NAME_KEY);
		int number = (Integer) data.get(NUMBER_KEY);
		String mac = ((String) data.get(MAC_KEY)).toLowerCase();
		return new PortData(name, number, mac);
	}
}
