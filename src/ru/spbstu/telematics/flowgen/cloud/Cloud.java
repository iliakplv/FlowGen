package ru.spbstu.telematics.flowgen.cloud;


import ru.spbstu.telematics.flowgen.application.configuration.*;
import ru.spbstu.telematics.flowgen.cloud.rabbitmq.NovaNetworkQueueListener;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;
import ru.spbstu.telematics.flowgen.openflow.floodlight.IFloodlightClient;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

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
	private IFloodlightClient floodlightClient = null;
	private Set<IDatapathListener> datapathListeners = null;
	private Map<NovaNetworkQueueListener, Thread> novaListeners = null;

	/**
	 * Constructors
	 */

	public Cloud(String name) {
		setName(name);
		dpidDatapathMap = new HashMap<String, IDatapath>();
		macActiveHostsMap = new HashMap<String, VmConnectionData>();
		macPausedHostsMap = new HashMap<String, VmConnectionData>();
		datapathListeners = new HashSet<IDatapathListener>();
		novaListeners = new HashMap<NovaNetworkQueueListener, Thread>();
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

		for (IDatapathListener listener : datapathListeners) {
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
		for (IDatapathListener listener : datapathListeners) {
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
		if (!datapathListeners.contains(listener)) {
			datapathListeners.add(listener);
			Collection<IDatapath> datapaths = dpidDatapathMap.values();
			for (IDatapath datapath : datapaths) {
				datapath.registerListener(listener);
			}
		}
	}

	@Override
	public void deleteDatapathListener(IDatapathListener listener) {
		if (datapathListeners.contains(listener)) {
			Collection<IDatapath> datapaths = dpidDatapathMap.values();
			for (IDatapath datapath : datapaths) {
				datapath.unregisterListener(listener);
			}
			datapathListeners.remove(listener);
		}
	}

	@Override
	public Set<IDatapathListener> getDatapathListeners() {
		return new HashSet<IDatapathListener>(datapathListeners);
	}

	@Override
	public void clearDatapathListeners() {
		Collection<IDatapath> datapaths = dpidDatapathMap.values();
		for (IDatapathListener listener : datapathListeners) {
			for (IDatapath datapath : datapaths) {
				datapath.unregisterListener(listener);
			}
		}
		datapathListeners.clear();
	}

	@Override
	public void launchGateway(String mac, String dpid, int port) {
		launchDevice(mac, dpid, port, OpenflowUtils.GATEWAY_FLOW_PRIORITY);
	}

	@Override
	public void launchHost(String mac, String dpid, int port) {
		launchDevice(mac, dpid, port, OpenflowUtils.HOST_FLOW_PRIORITY);
	}

	private void launchDevice(String mac, String dpid, int port, int priority) {
		dpid = dpid.toLowerCase();
		if (dpidDatapathMap.containsKey(dpid)) {
			mac = mac.toLowerCase();
			if (!(macActiveHostsMap.containsKey(mac) || macPausedHostsMap.containsKey(mac))) {
				IDatapath datapath = dpidDatapathMap.get(dpid);
				// priority value must be correct here
				datapath.connectHost(mac, port, priority);
				macActiveHostsMap.put(mac, new VmConnectionData(dpid, port));
			} else {
				throw new IllegalArgumentException("Cloud " + toString() + " already has host with MAC " + mac);
			}
		} else {
			throw new IllegalArgumentException("Cloud " + toString() + " has no datapath with DPID " + dpid);
		}
	}

	@Override
	public void pauseDevice(String mac) {
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
	public void wakeDevice(String mac) {
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
	public void stopDevice(String mac) {
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
	public void migrateDevice(String mac, String dstDpid, int dstPort) {
		// TODO implement
	}

	@Override
	public Set<String> getAllDevicesMacs() {
		Set<String> macs = macActiveHostsMap.keySet();
		macs.addAll(macPausedHostsMap.keySet());
		return macs;
	}

	@Override
	public DeviceState getDeviceState(String mac) {
		DeviceState result;

		mac = OpenflowUtils.setMacUniqueness(mac, true);
		result = getDeviceStateBySpecifiedMac(mac);
		// if globally unique MAC not found try to find locally unique MAC
		if (result == DeviceState.NotConnected) {
			mac = OpenflowUtils.setMacUniqueness(mac, false);
			result = getDeviceStateBySpecifiedMac(mac);
		}

		return result;
	}

	private DeviceState getDeviceStateBySpecifiedMac(String mac) {
		DeviceState result;

		if (macActiveHostsMap.containsKey(mac)) {
			result = DeviceState.Active;
		} else if (macPausedHostsMap.containsKey(mac)) {
			result = DeviceState.Paused;
		} else {
			result = DeviceState.NotConnected;
		}

		return result;
	}

	@Override
	public void setFloodlightClient(IFloodlightClient floodlightClient) {
		if (floodlightClient == null) {
			throw new NullPointerException("FloodlightClient is null in cloud " + toString());
		}
		this.floodlightClient = floodlightClient;
	}

	@Override
	public IFloodlightClient getFloodlightClient() {
		return floodlightClient;
	}

	@Override
	public void findAndConnect(String ip) {
		findAndDoAction(ip, ControllerHostConnector.Action.Connect);
	}

	@Override
	public void findAndDisconnect(String ip) {
		findAndDoAction(ip, ControllerHostConnector.Action.Disconnect);
	}

	private void findAndDoAction(String ip, ControllerHostConnector.Action action) {
		if (floodlightClient == null) {
			throw new NullPointerException("No floodlight client set to cloud " + toString());
		}
		if (StringUtils.isNullOrEmpty(ip)) {
			throw new IllegalArgumentException("Wrong IP (null or empty) in cloud " + toString());
		}

		Thread connectorThread =
				new Thread(new ControllerHostConnector(this, ip, action));
		connectorThread.start();

	}

	@Override
	public void addNovaListener(NovaNetworkQueueListener listener) {
		if (!novaListeners.containsKey(listener)) {
			listener.setCloud(this);
			Thread listenerThread = new Thread(listener);
			novaListeners.put(listener, listenerThread);
			listenerThread.start();
		}
	}

	@Override
	public void deleteNovaListener(NovaNetworkQueueListener listener) {
		if (novaListeners.containsKey(listener)) {
			Thread listenerThread = novaListeners.get(listener);
			novaListeners.remove(listener);
			listenerThread.interrupt();
		}
	}

	@Override
	public Set<NovaNetworkQueueListener> getNovaListeners() {
		return novaListeners.keySet();
	}

	@Override
	public void clearNovaListeners() {
		for (Thread thread : novaListeners.values()) {
			thread.interrupt();
		}
		novaListeners.clear();
	}

	@Override
	public CloudConfig getConfig() {

		FloodlightConfig floodlightConfig = null;
		if (floodlightClient != null) {
			 floodlightConfig = floodlightClient.getConfig();
		}

		CloudConfig cloudConfig = new CloudConfig(name, floodlightConfig);

		for (NovaNetworkQueueListener listener : novaListeners.keySet()) {
			ServerConfig serverConfig = listener.getConfig();
			cloudConfig.addServer(serverConfig);
		}

		for (IDatapath datapath : dpidDatapathMap.values()) {
			DatapathConfig datapathConfig = new DatapathConfig(datapath.getDpid(),
					datapath.getName(),
					datapath.getTrunkPort(),
					datapath.getFirewallPort(),
					datapath.getGatewayMac(),
					datapath.isConnectedToSubnet());

			Map<String, Integer> macPortMap = datapath.getMacPortMap();
			Map<String, Integer> macPriorityMap = datapath.getMacPriorityMap();

			for (String mac : macPortMap.keySet()) {
				boolean active = getDeviceState(mac) == DeviceState.Active;
				DeviceConfig deviceConfig = new DeviceConfig(mac, macPortMap.get(mac), active);

				if (macPriorityMap.get(mac) == OpenflowUtils.GATEWAY_FLOW_PRIORITY) {
					datapathConfig.addGateway(deviceConfig);
				} else {
					datapathConfig.addHost(deviceConfig);
				}
			}

			cloudConfig.addDatapath(datapathConfig);
		}

		return cloudConfig;
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
