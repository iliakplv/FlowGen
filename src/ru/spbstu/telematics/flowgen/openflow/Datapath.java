package ru.spbstu.telematics.flowgen.openflow;


import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Datapath implements IDatapath {

	public static final int INTERNAL_DATAPATH_TRUNK_PORT_NUMBER = 0;
	public static final int NO_FIREWALL_PORT_NUMBER = 0;

	private static final String NOT_INITIALIZED = "<not_initialized>";

	private String mDpid =			NOT_INITIALIZED;
	private String mName =			NOT_INITIALIZED;
	private int mTrunkPort =		INTERNAL_DATAPATH_TRUNK_PORT_NUMBER;
	private int mFirewallPort =		NO_FIREWALL_PORT_NUMBER;
	private List<String> mVmMacs;
	private List<String> mExternalMacs;
	private Map<String, Integer> mVmPorts;


	public Datapath(String dpid, String name, int trunkPort, int firewallPort) {
		setDpid(dpid);
		setName(name);
		setTrunkPort(trunkPort);
		setFirewallPort(firewallPort);
		mVmMacs = new LinkedList<String>();
		mExternalMacs = new LinkedList<String>();
		mVmPorts = new HashMap<String, Integer>();

	}

	public Datapath(String dpid, String name) {
		this(dpid, name, INTERNAL_DATAPATH_TRUNK_PORT_NUMBER, NO_FIREWALL_PORT_NUMBER);
	}


	/**
	 * DPID
	 */

	public String getDpid() {
		return mDpid;
	}

	private void setDpid(String dpid) {
		if (!OpenflowUtils.validateDpid(dpid)) {
			throw new IllegalArgumentException("Wrong DPID: " + dpid);
		}
		mDpid = dpid.toLowerCase();
	}


	/**
	 * Name
	 */

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		if (StringUtils.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("Datapath name is null or empty");
		}
		mName = name;
	}


	/**
	 * Ports
	 */

	public boolean isVmPort(int port) {
		return mVmPorts != null && mVmPorts.values().contains(port);
	}

	/**
	 * Trunk port
	 */

	public int getTrunkPort() {
		return mTrunkPort;
	}

	public void setTrunkPort(int trunkPort) {
		if (!(OpenflowUtils.validatePortNumber(trunkPort) || trunkPort == INTERNAL_DATAPATH_TRUNK_PORT_NUMBER)) {
			throw new IllegalArgumentException("Wrong trunk port number in datapath " + toString());
		}
		if (trunkPort != INTERNAL_DATAPATH_TRUNK_PORT_NUMBER && trunkPort == mFirewallPort) {
			throw new IllegalArgumentException("Trunk port equals to firewall port of datapath " + toString());
		}
		if (isVmPort(trunkPort)) {
			throw new IllegalArgumentException("Trunk port equals to one of VM ports of datapath " + toString());
		}
		mTrunkPort = trunkPort;
	}

	public boolean isConnectedToTrunk() {
		return mTrunkPort != INTERNAL_DATAPATH_TRUNK_PORT_NUMBER;
	}


	/**
	 * Firewall port
	 */

	public int getFirewallPort() {
		return mFirewallPort;
	}

	public void setFirewallPort(int firewallPort) {
		if (!(OpenflowUtils.validatePortNumber(firewallPort) || firewallPort == NO_FIREWALL_PORT_NUMBER)) {
			throw new IllegalArgumentException("Wrong firewall port number in datapath " + toString());
		}
		if (firewallPort != NO_FIREWALL_PORT_NUMBER && firewallPort == mTrunkPort) {
			throw new IllegalArgumentException("Firewall port equals to trunk port of datapath " + toString());
		}
		if (isVmPort(firewallPort)) {
			throw new IllegalArgumentException("Firewall port equals to one of VM ports of datapath " + toString());
		}
		mFirewallPort = firewallPort;
	}

	public boolean containsFirewallPort() {
		return mFirewallPort != NO_FIREWALL_PORT_NUMBER;
	}


	/**
	 * Virtual Machines
	 */
	// TODO rules for VMs
	// TODO rules for Gateway
	// TODO rules for Subnet

	public IFirewallRule connectVm(int port, String mac) {
		if (!OpenflowUtils.validatePortNumber(port)) {
			throw new IllegalArgumentException("Wrong port number: " + port);
		}
		if (port == mTrunkPort) {
			throw new IllegalArgumentException("VM port equals to trunk port of datapath " + toString());
		}
		if (port == mFirewallPort) {
			throw new IllegalArgumentException("VM port equals to firewall port of datapath " + toString());
		}
		if (isVmPort(port)) {
			throw new IllegalArgumentException("VM already connected to port " + port + " of datapath " + toString());
		}

		mac = mac.toLowerCase();
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong MAC: " + mac);
		}
		if (mVmMacs.contains(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") already connected to datapath " + toString());
		}
		if (mExternalMacs.contains(mac)) {
			throw new IllegalArgumentException("External host with such MAC (" + mac + ") found in datapath " + toString());
		}

		mVmPorts.put(mac, port);
		mVmMacs.add(mac);

		OnePortFirewallRule rule = null;
//		if (containsFirewallPort()) {
//			rule = new OnePortFirewallRule(mDpid, true, OpenflowUtils.MAX_FLOW_PRIORITY, mFirewallPort, port, mac);
//		}
		return rule;
	}

	public IFirewallRule disconnectVm(String mac) {
		mac = mac.toLowerCase();
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong MAC: " + mac);
		}
		if (!mVmMacs.contains(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") not connected to datapath " + toString());
		}

		mVmPorts.remove(mac);
		mVmMacs.remove(mac);

		OnePortFirewallRule rule = null;
//		if (containsFirewallPort()) {
//			rule = new OnePortFirewallRule(mDpid, true, OpenflowUtils.MAX_FLOW_PRIORITY, mFirewallPort, mVmPorts.get(mac), mac);
//		}
		return rule;
	}


	/**
	 * Other
	 */

	@Override
	public String toString() {
		return  mName + " (" + mDpid + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Datapath) {
			Datapath other = (Datapath) obj;
			return this == other || mDpid.equals(other.getDpid());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return mDpid.hashCode();
	}

}
