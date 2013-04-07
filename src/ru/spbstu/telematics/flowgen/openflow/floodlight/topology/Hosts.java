package ru.spbstu.telematics.flowgen.openflow.floodlight.topology;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Hosts {

	private Map<String, Host> hosts;


	public Hosts() {
		hosts = new HashMap<String, Host>();
	}

	public void addHost(Host host) {
		hosts.put(host.getMac().toLowerCase(), host);
	}

	public Host getHost(String mac) {
		return hosts.get(mac.toLowerCase());
	}

	public Set<String> getAllMacs() {
		return hosts.keySet();
	}

	public Collection<Host> getAllHosts() {
		return hosts.values();
	}

	public static Hosts parse(JSONArray data) throws JSONException {
		int length = data.length();
		Hosts hosts = new Hosts();
		for (int i = 0; i < length; i++) {
			Host host = Host.parse(data.getJSONObject(i));
			if (host != null) {
				hosts.addHost(host);
			}
		}
		return hosts;
	}


}
