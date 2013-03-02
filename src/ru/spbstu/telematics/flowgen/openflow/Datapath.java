package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Datapath implements IDatapath {

	private static final String NOT_INITIALIZED = "<not_initialized>";

	private String dpid =		NOT_INITIALIZED;
	private String name =		NOT_INITIALIZED;
	private int trunkPort =		-1;
	private int firewallPort =	-1;
	private String gatewayMac;
	private Map<Integer, String> portMacMap;
	private Map<String, Integer> macPortMap;


	public Datapath(String dpid, String name, int trunkPort, int firewallPort, String gatewayMac) {
		setDpid(dpid);
		setName(name);
		setTrunkPort(trunkPort);
		setFirewallPort(firewallPort);
		setGatewayMac(gatewayMac);
		portMacMap = new HashMap<Integer, String>();
		macPortMap = new HashMap<String, Integer>();
	}


	/**
	 * DPID
	 */

	public String getDpid() {
		return dpid;
	}

	private void setDpid(String dpid) {
		if (!OpenflowUtils.validateDpid(dpid)) {
			throw new IllegalArgumentException("Wrong DPID (" + dpid + ") of datapath with mane " + name);
		}
		this.dpid = dpid.toLowerCase();
	}


	/**
	 * Name
	 */

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!OpenflowUtils.validateDatapathName(name)) {
			throw new IllegalArgumentException("Wrong name (" + name + ") of datapath with ID " + dpid);
		}
		this.name = name;
	}


	/**
	 * Ports
	 */

	public boolean isVmPort(int port) {
		return portMacMap != null && portMacMap.keySet().contains(port);
	}

	/**
	 * Trunk port
	 */

	public int getTrunkPort() {
		return trunkPort;
	}

	public void setTrunkPort(int trunkPort) {
		if (!OpenflowUtils.validatePortNumber(trunkPort)) {
			throw new IllegalArgumentException("Wrong trunk port (" + trunkPort + ") in datapath " + toString());
		}
		if (trunkPort == firewallPort) {
			throw new IllegalArgumentException("New trunk port equals to firewall port (" + firewallPort + ") of datapath " + toString());
		}
		if (isVmPort(trunkPort)) {
			throw new IllegalArgumentException("New trunk port (" + trunkPort + ") equals to one of VM ports of datapath " + toString());
		}
		this.trunkPort = trunkPort;
	}


	/**
	 * Firewall port
	 */

	public int getFirewallPort() {
		return firewallPort;
	}

	public void setFirewallPort(int firewallPort) {
		if (!OpenflowUtils.validatePortNumber(firewallPort)) {
			throw new IllegalArgumentException("Wrong firewall port (" + firewallPort + ") in datapath " + toString());
		}
		if (firewallPort == trunkPort) {
			throw new IllegalArgumentException("New firewall port equals to trunk port (" + trunkPort + ") of datapath " + toString());
		}
		if (isVmPort(firewallPort)) {
			throw new IllegalArgumentException("New firewall port (" + firewallPort + ") equals to one of VM ports of datapath " + toString());
		}
		this.firewallPort = firewallPort;
	}

	/**
	 * MACs
	 */

	public boolean isVmMac(String mac) {
		return macPortMap != null && macPortMap.keySet().contains(mac.toLowerCase());
	}

	public boolean isGatewayMac(String mac) {
		return gatewayMac.equalsIgnoreCase(mac);
	}


	/**
	 * Gateway MAC
	 */

	public String getGatewayMac() {
		return gatewayMac;
	}

	public void setGatewayMac(String mac) {
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong gateway MAC (" + mac + ") in datapath " + toString());
		}
		if (isVmMac(mac)) {
			throw new IllegalArgumentException("New gateway MAC (" + mac + ") equals to one of the VM MACs of datapath " + toString());
		}
		gatewayMac = mac.toLowerCase();
	}


	/**
	 * IDatapath implementation
	 */

	@Override
	public JSONObject[] connectVm(String mac, int port) {
		mac = mac.toLowerCase();
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong VM MAC (" + mac + ") in datapath " + toString());
		}
		if (isGatewayMac(mac)) {
			throw new IllegalArgumentException("New VM MAC equals to gateway MAC (" + gatewayMac + ") of datapath " + toString());
		}
		if (isVmMac(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") already connected to port " +
					macPortMap.get(mac) + " of datapath " + toString());
		}

		if (!OpenflowUtils.validatePortNumber(port)) {
			throw new IllegalArgumentException("Wrong port number (" + port + ") to VM with MAC " + mac + " in datapath " + toString());
		}
		if (port == trunkPort) {
			throw new IllegalArgumentException("New VM port equals to trunk port (" + trunkPort + ") of datapath " + toString());
		}
		if (port == firewallPort) {
			throw new IllegalArgumentException("New VM port equals to firewall port (" + firewallPort + ") of datapath " + toString());
		}
		if (isVmPort(port)) {
			throw new IllegalArgumentException("Trying to connect VM with MAC " + mac + " to port " + port +
					". VM with MAC " + portMacMap.get(port) + " already connected to this port of datapath " + toString());
		}

		portMacMap.put(port, mac);
		macPortMap.put(mac, port);
		IFirewallRule rule = getVmRule(port);
		return getCommands(rule, true);
	}

	@Override
	public JSONObject[] disconnectVm(String mac) {
		if(!isVmMac(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") not connected to datapath " + toString());
		}
		IFirewallRule rule = getVmRule(mac);
		portMacMap.remove(macPortMap.get(mac));
		macPortMap.remove(mac);
		return getCommands(rule, false);
	}

	@Override
	public JSONObject[] disconnectVm(int port) {
		if (!isVmPort(port)) {
			throw new IllegalArgumentException("No VM connected to port " + port + " of datapath " + toString());
		}

		IFirewallRule rule = getVmRule(port);
		macPortMap.remove(portMacMap.get(port));
		portMacMap.remove(port);
		return getCommands(rule, false);
	}

	@Override
	public IFirewallRule getVmRule(String mac) {
		if (isVmMac(mac)) {
			return new OnePortFirewallVmRule(dpid, firewallPort, macPortMap.get(mac), mac);
		}
		return null;
	}

	@Override
	public IFirewallRule getVmRule(int port) {
		if (isVmPort(port)) {
			return new OnePortFirewallVmRule(dpid, firewallPort, port, portMacMap.get(port));
		}
		return null;
	}

	@Override
	public List<IFirewallRule> getAllVmRules() {
		ArrayList<IFirewallRule> rules = new ArrayList<IFirewallRule>();
		Set<Integer> ports = portMacMap.keySet();
		for (int port : ports) {
			rules.add(getVmRule(port));
		}
		return rules;
	}

	@Override
	public JSONObject[] connectGateway() {
		return getCommands(getGatewayRule(), true);
	}

	@Override
	public JSONObject[] disconnectGateway() {
		return getCommands(getGatewayRule(), false);
	}

	@Override
	public IFirewallRule getGatewayRule() {
		return new OnePortFirewallGatewayRule(dpid, firewallPort, trunkPort, gatewayMac);
	}

	@Override
	public JSONObject[] connectSubnet() {
		return getCommands(getSubnetRule(), true);
	}

	@Override
	public JSONObject[] disconnectSubnet() {
		return getCommands(getSubnetRule(), false);
	}

	@Override
	public IFirewallRule getSubnetRule() {
		return new OnePortFirewallSubnetRule(dpid, firewallPort, trunkPort);
	}

	@Override
	public List<IFirewallRule> getAllRules() {
		List<IFirewallRule> rules = getAllVmRules();
		rules.add(getGatewayRule());
		rules.add(getSubnetRule());
		return rules;
	}


	/**
	 * Other
	 */

	private static JSONObject[] getCommands(IFirewallRule rule, boolean connect) {
		JSONObject[] commands;
		if (rule instanceof OnePortFirewallSubnetRule) {
			commands = new JSONObject[1];
			if (connect) {
				commands[0] = rule.ovsOutFlowAddCommand();
			} else {
				commands[0] = rule.ovsOutFlowRemoveCommand();
			}
		} else {
			commands = new JSONObject[2];
			if (connect) {
				commands[0] = rule.ovsInFlowAddCommand();
				commands[1] = rule.ovsOutFlowAddCommand();
			} else {
				commands[0] = rule.ovsInFlowRemoveCommand();
				commands[1] = rule.ovsOutFlowRemoveCommand();
			}
		}
		return commands;
	}

	@Override
	public String toString() {
		return name + " (" + dpid + ")";
	}

}
