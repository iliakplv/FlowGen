package ru.spbstu.telematics.flowgen.cloud;


import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Cloud implements ICloud {

	private String name;

	private Map<String, IDatapath> dpidDatapathMap;
	private Map<String, VmConnectionData> macActiveVmMap;
	private Map<String, VmConnectionData> macPausedVmMap;

	/**
	 * Constructors
	 */

	public Cloud(String name) {
		setName(name);
		dpidDatapathMap = new HashMap<String, IDatapath>();
		macActiveVmMap = new HashMap<String, VmConnectionData>();
		macPausedVmMap = new HashMap<String, VmConnectionData>();
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
	}

	@Override
	public void deleteDatapath(String dpid) {
		dpid = dpid.toLowerCase();
		if (!dpidDatapathMap.containsKey(dpid)) {
			throw new IllegalArgumentException("Cloud " + toString() + " has no datapath with DPID " + dpid);
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
	}

	// TODO check for datapath port availability when waking VM up

	@Override
	public void launchVm(String mac, String dpid, int port) {
		dpid = dpid.toLowerCase();
		if (dpidDatapathMap.containsKey(dpid)) {
			mac = mac.toLowerCase();
			if (!(macActiveVmMap.containsKey(mac) || macPausedVmMap.containsKey(mac))) {
				IDatapath datapath = dpidDatapathMap.get(dpid);
				datapath.connectVm(mac, port);
				macActiveVmMap.put(mac, new VmConnectionData(dpid, port));
			} else {
				throw new IllegalArgumentException("Cloud " + toString() + " already has VM with MAC " + mac);
			}
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no datapath with DPID " + dpid);
		}
	}

	@Override
	public void pauseVm(String mac) {
		mac = mac.toLowerCase();
		if (macActiveVmMap.containsKey(mac)) {
			VmConnectionData vmData = macActiveVmMap.get(mac);
			IDatapath datapath = dpidDatapathMap.get(vmData.getDpid());
			datapath.disconnectVm(mac);
			macPausedVmMap.put(mac, vmData);
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no active VM with MAC " + mac);
		}
	}

	@Override
	public void wakeVm(String mac) {
		mac = mac.toLowerCase();
		if (macPausedVmMap.containsKey(mac)) {
			VmConnectionData vmData = macPausedVmMap.get(mac);
			IDatapath datapath = dpidDatapathMap.get(vmData.getDpid());
			datapath.connectVm(mac, vmData.getPort());
			macActiveVmMap.put(mac, vmData);
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no paused VM with MAC " + mac);
		}

	}

	@Override
	public void stopVm(String mac) {
		mac = mac.toLowerCase();
		if (macActiveVmMap.containsKey(mac)) {
			VmConnectionData vmData = macActiveVmMap.get(mac);
			IDatapath datapath = dpidDatapathMap.get(vmData.getDpid());
			datapath.disconnectVm(mac);
			macActiveVmMap.remove(mac);
		} else if (macPausedVmMap.containsKey(mac)) {
			macPausedVmMap.remove(mac);
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no active VM with MAC " + mac);
		}

	}

	@Override
	public void migrateVm(String mac, String dstDpid, int dstPort) {
		// TODO
	}

	@Override
	public Set<String> getAllVmMacs() {
		Set<String> macs = macActiveVmMap.keySet();
		macs.addAll(macPausedVmMap.keySet());
		return macs;
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
