package ru.spbstu.telematics.flowgen.openflow.floodlight.topology;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.StringUtils;


public class Host {

	public static final String ATTACHMENT_POINT_KEY = "attachmentPoint";
	public static final String MAC_KEY = "mac";
	public static final String IPV4_KEY = "ipv4";

	private String mac;
	private String ipv4;
	private AttachmentPoint attachmentPoint;


	public Host(String mac, String ipv4, AttachmentPoint attachmentPoint) {
		this.mac = mac;
		this.ipv4 = ipv4;
		this.attachmentPoint = attachmentPoint;
	}


	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getIpv4() {
		return ipv4;
	}

	public void setIpv4(String ipv4) {
		this.ipv4 = ipv4;
	}

	public AttachmentPoint getAttachmentPoint() {
		return attachmentPoint;
	}

	public void setAttachmentPoint(AttachmentPoint attachmentPoint) {
		this.attachmentPoint = attachmentPoint;
	}


	public static Host parse(JSONObject data) throws JSONException {
		Host result = null
				;
		JSONArray points = data.getJSONArray(ATTACHMENT_POINT_KEY);
		if (points.length() > 0) {

			JSONArray macs = (JSONArray) data.get(MAC_KEY);
			JSONArray ips = (JSONArray) data.get(IPV4_KEY);

			if (macs.length() > 0 && ips.length() > 0) {
				String mac = macs.getString(0);
				String ip = ips.getString(0);
				AttachmentPoint attachmentPoint = AttachmentPoint.parse(points.getJSONObject(0));
				result = new Host(mac, ip, attachmentPoint);
			}
		}

		return result;
	}
}
