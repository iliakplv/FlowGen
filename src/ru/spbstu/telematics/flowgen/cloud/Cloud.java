package ru.spbstu.telematics.flowgen.cloud;


import org.json.JSONException;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;
import ru.spbstu.telematics.flowgen.openflow.floodlight.IFloodlightClient;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.ControllerData;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.DatapathData;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.PortData;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Cloud implements ICloud {

	private String name;

	private Map<String, IDatapath> dpidDatapathMap;
	private Map<String, VmConnectionData> macActiveHostsMap;
	private Map<String, VmConnectionData> macPausedHostsMap;
	private Set<IDatapathListener> listeners = null;
	private IFloodlightClient floodlightClient = null;

	/**
	 * Constructors
	 */

	public Cloud(String name) {
		setName(name);
		dpidDatapathMap = new HashMap<String, IDatapath>();
		macActiveHostsMap = new HashMap<String, VmConnectionData>();
		macPausedHostsMap = new HashMap<String, VmConnectionData>();
		listeners = new HashSet<IDatapathListener>();
	}


	/**
	 * Cloud
	 */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException("Cloud name is null");
		}
		this.name = name;
	}


	/**
	 * ICLoud implementation
	 */

	@Override
	public void addDatapath(IDatapath datapath) {
		String dpid = datapath.getDpid().toLowerCase();
		if (dpidDatapathMap.containsKey(dpid)) {
			throw new IllegalArgumentException("Cloud " + toString() + " already contains datapath " + datapath.toString());
		}
		dpidDatapathMap.put(dpid, datapath);

		for (IDatapathListener listener : listeners) {
			datapath.registerListener(listener);
		}
	}

	@Override
	public void deleteDatapath(String dpid) {
		dpid = dpid.toLowerCase();
		if (!dpidDatapathMap.containsKey(dpid)) {
			throw new IllegalArgumentException("Cloud " + toString() + " has no datapath with DPID " + dpid);
		}

		IDatapath datapath = getDatapath(dpid);
		for (IDatapathListener listener : listeners) {
			datapath.unregisterListener(listener);
		}

		dpidDatapathMap.remove(dpid);
	}

	@Override
	public IDatapath getDatapath(String dpid) {
		return dpidDatapathMap.get(dpid.toLowerCase());
	}

	@Override
	public Set<String> getAllDpids() {
		return dpidDatapathMap.keySet();
	}

	@Override
	public void addDatapathListener(IDatapathListener listener) {
		listeners.add(listener);
		Collection<IDatapath> datapaths = dpidDatapathMap.values();
		for (IDatapath datapath : datapaths) {
			datapath.registerListener(listener);
		}
	}

	@Override
	public void deleteDatapathListener(IDatapathListener listener) {
		Collection<IDatapath> datapaths = dpidDatapathMap.values();
		for (IDatapath datapath : datapaths) {
			datapath.unregisterListener(listener);
		}
		listeners.remove(listener);
	}

	@Override
	public Set<IDatapathListener> getAllListeners() {
		return new HashSet<IDatapathListener>(listeners);
	}

	@Override
	public void clearListeners() {
		Collection<IDatapath> datapaths = dpidDatapathMap.values();
		for (IDatapathListener listener : listeners) {
			for (IDatapath datapath : datapaths) {
				datapath.unregisterListener(listener);
			}
		}
		listeners.clear();
	}

	// TODO check for datapath port availability when waking VM up

	@Override
	public void launchHost(String mac, String dpid, int port) {
		dpid = dpid.toLowerCase();
		if (dpidDatapathMap.containsKey(dpid)) {
			mac = mac.toLowerCase();
			if (!(macActiveHostsMap.containsKey(mac) || macPausedHostsMap.containsKey(mac))) {
				IDatapath datapath = dpidDatapathMap.get(dpid);
				datapath.connectHost(mac, port);
				macActiveHostsMap.put(mac, new VmConnectionData(dpid, port));
			} else {
				throw new IllegalArgumentException("Cloud " + toString() + " already has VM with MAC " + mac);
			}
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no datapath with DPID " + dpid);
		}
	}

	@Override
	public void pauseHost(String mac) {
		mac = mac.toLowerCase();
		if (macActiveHostsMap.containsKey(mac)) {
			VmConnectionData vmData = macActiveHostsMap.get(mac);
			IDatapath datapath = dpidDatapathMap.get(vmData.getDpid());
			datapath.disconnectHost(mac);
			macPausedHostsMap.put(mac, vmData);
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no active VM with MAC " + mac);
		}
	}

	@Override
	public void wakeHost(String mac) {
		mac = mac.toLowerCase();
		if (macPausedHostsMap.containsKey(mac)) {
			VmConnectionData vmData = macPausedHostsMap.get(mac);
			IDatapath datapath = dpidDatapathMap.get(vmData.getDpid());
			datapath.connectHost(mac, vmData.getPort());
			macActiveHostsMap.put(mac, vmData);
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no paused VM with MAC " + mac);
		}

	}

	@Override
	public void stopHost(String mac) {
		mac = mac.toLowerCase();
		if (macActiveHostsMap.containsKey(mac)) {
			VmConnectionData vmData = macActiveHostsMap.get(mac);
			IDatapath datapath = dpidDatapathMap.get(vmData.getDpid());
			datapath.disconnectHost(mac);
			macActiveHostsMap.remove(mac);
		} else if (macPausedHostsMap.containsKey(mac)) {
			macPausedHostsMap.remove(mac);
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no active VM with MAC " + mac);
		}

	}

	@Override
	public void migrateHost(String mac, String dstDpid, int dstPort) {
		// TODO implement
	}

	@Override
	public Set<String> getAllHostsMacs() {
		Set<String> macs = macActiveHostsMap.keySet();
		macs.addAll(macPausedHostsMap.keySet());
		return macs;
	}

	@Override
	public void setFloodlightClient(IFloodlightClient floodlightClient) {
		this.floodlightClient = floodlightClient;
	}

	@Override
	public IFloodlightClient getFloodlightClient() {
		return floodlightClient;
	}

	@Override
	public void removeFloodlightClient() {
		floodlightClient = null;
	}

	@Override
	public boolean launchVmByMac(String mac) {
		if (floodlightClient == null) {
			throw new NullPointerException("No floodlight client set to cloud " + toString());
		}
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong VM MAC (" + mac + ") in cloud " + toString());
		}
		mac = mac.toLowerCase();

		ControllerData controllerData;
		try {
			 controllerData = ControllerData.parse(floodlightClient.getAllConnectedHosts());
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}

		String dpid = null;
		int port = OpenflowUtils.DEFAULT_PORT;

		for (DatapathData datapathData : controllerData.getDatapaths()) {
			for (PortData portData : datapathData.getPorts()) {
				if (OpenflowUtils.macEquals(mac, portData.getMac()) && !portData.isDatapathReservedPort() ) {
					dpid = datapathData.getDpid();
					port = portData.getNumber();
				}
			}
		}

		boolean result = dpid != null && port != OpenflowUtils.DEFAULT_PORT;

		if (result) {
			launchHost(mac, dpid, port);
		}

		return result;
	}


	/**
	 * Other
	 */

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Inner classes
	 */

	private class VmConnectionData {

		private String dpid;
		private int port;


		private VmConnectionData(String dpid, int port) {
			setDpid(dpid);
			setPort(port);
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
	}
}
