package ru.spbstu.telematics.flowgen.application.configuration;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;

import java.util.ArrayList;
import java.util.List;

public class DatapathConfig {

	public static final String DPID_KEY = "dpid";
	public static final String NAME_KEY = "name";
	public static final String TRUNK_PORT_KEY = "trunk_port";
	public static final String FIREWALL_PORT_KEY = "firewall_port";
	public static final String GATEWAY_MAC_KEY = "gateway_mac";
	public static final String SUBNET_KEY = "connect_to_subnet";
	public static final String HOSTS_KEY = "hosts";
	public static final String GATEWAYS_KEY = "gateways";

	private String dpid;
	private String name;
	private int trunkPort;
	private int firewallPort;
	private String gatewayMac;
	private boolean subnet;
	private List<DeviceConfig> hosts;
	private List<DeviceConfig> gateways;


	public DatapathConfig(String dpid, String name, int trunkPort, int firewallPort, String gatewayMac, boolean subnet) {
		this.dpid = dpid;
		this.name = name;
		this.trunkPort = trunkPort;
		this.firewallPort = firewallPort;
		this.gatewayMac = gatewayMac;
		this.subnet = subnet;
		hosts = new ArrayList<DeviceConfig>();
		gateways = new ArrayList<DeviceConfig>();
	}

	public String getDpid() {
		return dpid;
	}

	public void setDpid(String dpid) {
		this.dpid = dpid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTrunkPort() {
		return trunkPort;
	}

	public void setTrunkPort(int trunkPort) {
		this.trunkPort = trunkPort;
	}

	public int getFirewallPort() {
		return firewallPort;
	}

	public void setFirewallPort(int firewallPort) {
		this.firewallPort = firewallPort;
	}

	public String getGatewayMac() {
		return gatewayMac;
	}

	public void setGatewayMac(String gatewayMac) {
		this.gatewayMac = gatewayMac;
	}

	public boolean isConnectedToSubnet() {
		return subnet;
	}

	public void setConnectedToSubnet(boolean subnet) {
		this.subnet = subnet;
	}

	public void addHost(DeviceConfig deviceConfig) {
		if (deviceConfig != null) {
			hosts.add(deviceConfig);
		}
	}

	public void addGateway(DeviceConfig deviceConfig) {
		if (deviceConfig != null) {
			gateways.add(deviceConfig);
		}
	}

	public List<DeviceConfig> getHosts() {
		return new ArrayList<DeviceConfig>(hosts);
	}

	public List<DeviceConfig> getGateways() {
		return new ArrayList<DeviceConfig>(gateways);
	}


	public JSONObject export() {
		JSONObject result = new JSONObject();

		try {
			result.accumulate(DPID_KEY, dpid);
			result.accumulate(NAME_KEY, name);
			result.accumulate(TRUNK_PORT_KEY, trunkPort);
			result.accumulate(FIREWALL_PORT_KEY, firewallPort);
			result.accumulate(GATEWAY_MAC_KEY, gatewayMac);
			result.accumulate(SUBNET_KEY, subnet);

			JSONArray gateways = new JSONArray();
			for (DeviceConfig device : this.gateways) {
				gateways.put(device.export());
			}
			result.accumulate(GATEWAYS_KEY, gateways);

			JSONArray hosts = new JSONArray();
			for (DeviceConfig device : this.hosts) {
				hosts.put(device.export());
			}
			result.accumulate(HOSTS_KEY, hosts);

		} catch (JSONException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	public static DatapathConfig parse(JSONObject data) {
		DatapathConfig result = null;

		try {
			String dpid = (String) data.get(DPID_KEY);
			String name = (String) data.get(NAME_KEY);
			int trunk = (Integer) data.get(TRUNK_PORT_KEY);
			int firewall = (Integer) data.get(FIREWALL_PORT_KEY);
			String gatewayMac = (String) data.get(GATEWAY_MAC_KEY);
			boolean subnet = (Boolean) data.get(SUBNET_KEY);

			if (OpenflowUtils.validateDpid(dpid) &&
					OpenflowUtils.validateDatapathName(name) &&
					OpenflowUtils.validatePortNumber(trunk) &&
					OpenflowUtils.validatePortNumber(firewall) &&
					OpenflowUtils.validateMac(gatewayMac)) {
				result = new DatapathConfig(dpid, name, trunk, firewall, gatewayMac, subnet);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (result == null) {
			return null;
		}

		try {

			JSONArray gateways = data.getJSONArray(GATEWAYS_KEY);
			JSONArray hosts = data.getJSONArray(HOSTS_KEY);

			for (int i = 0; i < gateways.length(); i++) {
				result.addGateway(DeviceConfig.parse(gateways.getJSONObject(i)));
			}
			for (int i = 0; i < hosts.length(); i++) {
				result.addHost(DeviceConfig.parse(hosts.getJSONObject(i)));
			}

		} catch (JSONException e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}


}
