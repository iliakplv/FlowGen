package ru.spbstu.telematics.flowgen.openflow;


import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Datapath implements IDatapath {

	private static final String NOT_INITIALIZED = "<not_initialized>";

	private String mDpid =			NOT_INITIALIZED;
	private String mName =			NOT_INITIALIZED;
	private int mTrunkPort =		-1;
	private int mFirewallPort =		-1;
	private String mGatewayMac;
	private Map<Integer, String> mPortMacMap;
	private Map<String, Integer> mMacPortMap;

	// TODO 1 get_Rule()
	// TODO 2 connect_() + disconnect_()
	// TODO 3 getAllVmRules()
	// TODO 4 getAllRules()


	public Datapath(String dpid, String name, int trunkPort, int firewallPort, String gatewayMac) {
		setDpid(dpid);
		setName(name);
		setTrunkPort(trunkPort);
		setFirewallPort(firewallPort);
		setGatewayMac(gatewayMac);
		mPortMacMap = new HashMap<Integer, String>();
		mMacPortMap = new HashMap<String, Integer>();
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
		return mPortMacMap != null && mPortMacMap.keySet().contains(port);
	}

	/**
	 * Trunk port
	 */

	public int getTrunkPort() {
		return mTrunkPort;
	}

	public void setTrunkPort(int trunkPort) {
		if (!OpenflowUtils.validatePortNumber(trunkPort)) {
			throw new IllegalArgumentException("Wrong trunk port number in datapath " + toString());
		}
		if (trunkPort == mFirewallPort) {
			throw new IllegalArgumentException("Trunk port equals to firewall port of datapath " + toString());
		}
		if (isVmPort(trunkPort)) {
			throw new IllegalArgumentException("Trunk port equals to one of VM ports of datapath " + toString());
		}
		mTrunkPort = trunkPort;
	}


	/**
	 * Firewall port
	 */

	public int getFirewallPort() {
		return mFirewallPort;
	}

	public void setFirewallPort(int firewallPort) {
		if (!OpenflowUtils.validatePortNumber(firewallPort)) {
			throw new IllegalArgumentException("Wrong firewall port number in datapath " + toString());
		}
		if (firewallPort == mTrunkPort) {
			throw new IllegalArgumentException("Firewall port equals to trunk port of datapath " + toString());
		}
		if (isVmPort(firewallPort)) {
			throw new IllegalArgumentException("Firewall port equals to one of VM ports of datapath " + toString());
		}
		mFirewallPort = firewallPort;
	}

	/**
	 * MACs
	 */

	public boolean isVmMac(String mac) {
		return mMacPortMap != null && mMacPortMap.keySet().contains(mac.toLowerCase());
	}

	public boolean isGatewayMac(String mac) {
		return mGatewayMac.equalsIgnoreCase(mac);
	}


	/**
	 * Gateway MAC
	 */

	public String getGatewayMac() {
		return mGatewayMac;
	}

	public void setGatewayMac(String mac) {
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong gateway MAC (" + mac + ") in datapath " + toString());
		}
		if (isVmMac(mac)) {
			throw new IllegalArgumentException("Gateway MAC equals to one of the VM MACs of datapath " + toString());
		}
		mGatewayMac = mac.toLowerCase();
	}


	/**
	 * IDatapath
	 */

	// TODO VMs

	@Override
	public IFirewallRule connectVm(String mac, int port) {
		mac = mac.toLowerCase();
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong VM MAC in datapath " + toString());
		}
		if (isGatewayMac(mac)) {
			throw new IllegalArgumentException("VM MAC equals to gateway MAC of datapath " + toString());
		}
		if (isVmMac(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") already connected to port " +
					mMacPortMap.get(mac) + " of datapath " + toString());
		}

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

		mPortMacMap.put(port, mac);
		mMacPortMap.put(mac, port);

		return getVmRule(port);
	}

	@Override
	public IFirewallRule disconnectVm(String mac) {
		if(!isVmMac(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") not connected to datapath " + toString());
		}
		return disconnectVm(mMacPortMap.get(mac));
	}

	@Override
	public IFirewallRule disconnectVm(int port) {
		if (!isVmPort(port)) {
			throw new IllegalArgumentException("No VM connected to port " + port + " of datapath " + toString());
		}
		IFirewallRule rule = getVmRule(port);
		mMacPortMap.remove(mPortMacMap.get(port));
		mPortMacMap.remove(port);
		return rule;
	}

	@Override
	public IFirewallRule getVmRule(String mac) {
		if (isVmMac(mac)) {
			return new OnePortFirewallVmRule(mDpid, mFirewallPort, mMacPortMap.get(mac), mac);
		}
		return null;
	}

	@Override
	public IFirewallRule getVmRule(int port) {
		if (isVmPort(port)) {
			return new OnePortFirewallVmRule(mDpid, mFirewallPort, port, mPortMacMap.get(port));
		}
		return null;
	}

	@Override
	public List<IFirewallRule> getAllVmRules() {
		ArrayList<IFirewallRule> rules = new ArrayList<IFirewallRule>();
		Set<Integer> ports = mPortMacMap.keySet();
		for (int port : ports) {
			rules.add(getVmRule(port));
		}
		return rules;
	}

	// TODO Gateway

	@Override
	public IFirewallRule connectGateway() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public IFirewallRule disconnectGateway() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public IFirewallRule getGatewayRule() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	// TODO Subnet

	@Override
	public IFirewallRule connectSubnet() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public IFirewallRule disconnectSubnet() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public IFirewallRule getSubnetRule() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	// TODO all

	@Override
	public List<IFirewallRule> getAllRules() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}


	/**
	 * Other
	 */

	@Override
	public String toString() {
		return mName + " (" + mDpid + ")";
	}

}
