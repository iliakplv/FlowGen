package ru.spbstu.telematics.flowgen.openflow.floodlight.topology;


import org.json.JSONException;
import org.json.JSONObject;

public class AttachmentPoint {

	public static final String DPID_KEY = "switchDPID";
	public static final String PORT_KEY = "port";

	private String dpid;
	private int port;


	public AttachmentPoint(String dpid, int port) {
		this.dpid = dpid;
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDpid() {
		return dpid;
	}

	public void setDpid(String dpid) {
		this.dpid = dpid;
	}

	public static AttachmentPoint parse(JSONObject data) throws JSONException {
		String dpid = (String) data.get(DPID_KEY);
		int port = (Integer) data.get(PORT_KEY);
		return new AttachmentPoint(dpid, port);
	}
}
