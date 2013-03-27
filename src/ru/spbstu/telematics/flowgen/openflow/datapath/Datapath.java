package ru.spbstu.telematics.flowgen.openflow.datapath;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.openflow.rules.CommandType;
import ru.spbstu.telematics.flowgen.openflow.rules.IFirewallRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallBroadcastRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallGatewayRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallSubnetRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallVmRule;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Datapath implements IDatapath {

	private static final String NOT_INITIALIZED = "<not_initialized>";

	// Datapath params
	private String dpid = NOT_INITIALIZED;
	private String name = NOT_INITIALIZED;
	private int firewallPort = OpenflowUtils.DEFAULT_PORT;

	// Network params
	private boolean connectedToNetwork = false;
	private String gatewayMac = null;
	private int trunkPort = OpenflowUtils.DEFAULT_PORT;

	// VM
	private Map<String, Integer> macPortMap;

	// Listeners
	private LinkedList<IDatapathListener> listeners;


	/**
	 * Constructors
	 */

	public Datapath(String dpid, String name, int trunkPort, int firewallPort, String gatewayMac) {
		setDpid(dpid);
		setName(name);
		setTrunkPort(trunkPort);
		setFirewallPort(firewallPort);
		setGatewayMac(gatewayMac);
		macPortMap = new HashMap<String, Integer>();
		listeners = new LinkedList<IDatapathListener>();
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

	private void setName(String name) {
		if (!OpenflowUtils.validateDatapathName(name)) {
			throw new IllegalArgumentException("Wrong name (" + name + ") of datapath with ID " + dpid);
		}
		this.name = name;
	}


	/**
	 * Trunk port
	 */

	@Override
	public int getTrunkPort() {
		return trunkPort;
	}

	private void setTrunkPort(int trunkPort) {
		if (!OpenflowUtils.validatePortNumber(trunkPort)) {
			throw new IllegalArgumentException("Wrong trunk port (" + trunkPort + ") in datapath " + toString());
		}
		if (trunkPort == firewallPort) {
			throw new IllegalArgumentException("New trunk port equals to firewall port (" + firewallPort + ") of datapath " + toString());
		}
		this.trunkPort = trunkPort;
	}


	/**
	 * Firewall port
	 */

	@Override
	public int getFirewallPort() {
		return firewallPort;
	}

	private void setFirewallPort(int firewallPort) {
		if (!OpenflowUtils.validatePortNumber(firewallPort)) {
			throw new IllegalArgumentException("Wrong firewall port (" + firewallPort + ") in datapath " + toString());
		}
		if (firewallPort == trunkPort) {
			throw new IllegalArgumentException("New firewall port equals to trunk port (" + trunkPort + ") of datapath " + toString());
		}
		this.firewallPort = firewallPort;
	}


	/**
	 * Gateway MAC
	 */

	@Override
	public String getGatewayMac() {
		return gatewayMac;
	}

	@Override
	public boolean isGatewayMac(String mac) {
		return gatewayMac.equalsIgnoreCase(mac);
	}

	private void setGatewayMac(String mac) {
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong gateway MAC (" + mac + ") in datapath " + toString());
		}
		gatewayMac = mac.toLowerCase();
	}


	/**
	 * VM
	 */

	@Override
	public boolean containsVm(String mac) {
		return macPortMap.containsKey(mac.toLowerCase());
	}

	@Override
	public int getVmPort(String mac) {
		int result;

		if (containsVm(mac)) {
			result = macPortMap.get(mac.toLowerCase());
		} else {
			result = OpenflowUtils.DEFAULT_PORT;
		}

		return result;
	}

	@Override
	public Map<String, Integer> getVmTopology() {
		return new HashMap<String, Integer>(macPortMap);
	}


	/**
	 * IDatapath implementation
	 */

	@Override
	public synchronized void connectVm(String mac, int port) {
		final CommandType commandType = CommandType.FLOW_ADD;

		if (mac == null) {
			throw new IllegalArgumentException("VM MAC is null in datapath " + toString());
		}
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong VM MAC (" + mac + ") in datapath " + toString());
		}
		if (isGatewayMac(mac)) {
			throw new IllegalArgumentException("New VM MAC equals to gateway MAC (" + gatewayMac + ") of datapath " + toString());
		}
		if (containsVm(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") already connected to port " +
					macPortMap.get(mac.toLowerCase()) + " of datapath " + toString());
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

		mac = mac.toLowerCase();
		macPortMap.put(mac, port);
		IFirewallRule rule = getVmRule(mac);
		JSONObject[] commands = createCommands(rule, commandType);
		notifyListeners(commands, commandType);
	}

	@Override
	public synchronized void disconnectVm(String mac) {
		final CommandType commandType = CommandType.FLOW_REMOVE;

		if(!containsVm(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") not connected to datapath " + toString());
		}

		mac = mac.toLowerCase();
		IFirewallRule rule = getVmRule(mac);
		macPortMap.remove(mac);
		JSONObject[] commands = createCommands(rule, commandType);
		notifyListeners(commands, commandType);
	}

	@Override
	public IFirewallRule getVmRule(String mac) {
		mac = mac.toLowerCase();
		if (containsVm(mac)) {
			return new OnePortFirewallVmRule(dpid, firewallPort, macPortMap.get(mac), mac);
		}
		return null;
	}

	@Override
	public List<IFirewallRule> getAllVmRules() {
		ArrayList<IFirewallRule> rules = new ArrayList<IFirewallRule>();
		Set<String> macs = macPortMap.keySet();
		for (String mac : macs) {
			rules.add(getVmRule(mac));
		}
		return rules;
	}

	// Network

	private void connectGateway() {
		final CommandType commandType = CommandType.FLOW_ADD;
		JSONObject[] commands = createCommands(getGatewayRule(), commandType);
		notifyListeners(commands, commandType);
	}

	private void disconnectGateway() {
		final CommandType commandType = CommandType.FLOW_REMOVE;
		JSONObject[] commands = createCommands(getGatewayRule(), commandType);
		notifyListeners(commands, commandType);
	}

	private IFirewallRule getGatewayRule() {
		return new OnePortFirewallGatewayRule(dpid, firewallPort, trunkPort, gatewayMac);
	}

	private void connectBroadcast() {
		final CommandType commandType = CommandType.FLOW_ADD;
		JSONObject[] commands = createCommands(getBroadcastRule(), commandType);
		notifyListeners(commands, commandType);
	}

	private void disconnectBroadcast() {
		final CommandType commandType = CommandType.FLOW_REMOVE;
		JSONObject[] commands = createCommands(getBroadcastRule(), commandType);
		notifyListeners(commands, commandType);
	}

	private IFirewallRule getBroadcastRule() {
		return new OnePortFirewallBroadcastRule(dpid, firewallPort);
	}

	private void connectSubnet() {
		final CommandType commandType = CommandType.FLOW_ADD;
		JSONObject[] commands = createCommands(getSubnetRule(), commandType);
		notifyListeners(commands, commandType);
	}

	private void disconnectSubnet() {
		final CommandType commandType = CommandType.FLOW_REMOVE;
		JSONObject[] commands = createCommands(getSubnetRule(), commandType);
		notifyListeners(commands, commandType);
	}

	private IFirewallRule getSubnetRule() {
		return new OnePortFirewallSubnetRule(dpid, firewallPort, trunkPort);
	}

	@Override
	public synchronized void connectToNetwork() {
		connectGateway();
		connectBroadcast();
		connectSubnet();
		connectedToNetwork = true;
	}

	@Override
	public synchronized void disconnectFromNetwork() {
		connectedToNetwork = false;
		disconnectSubnet();
		disconnectBroadcast();
		disconnectGateway();
	}

	@Override
	public boolean isConnectedToNetwork() {
		return connectedToNetwork;
	}

	@Override
	public List<IFirewallRule> getAllNetworkRules() {
		List<IFirewallRule> rules = new ArrayList<IFirewallRule>();
		rules.add(getGatewayRule());
		rules.add(getBroadcastRule());
		rules.add(getSubnetRule());
		return rules;

	}

	@Override
	public List<IFirewallRule> getAllRules() {
		List<IFirewallRule> rules = getAllNetworkRules();
		List<IFirewallRule> vmRules = getAllVmRules();
		rules.addAll(vmRules);
		return rules;
	}

	// VM Migration

	@Override
	public void migrateVm(String vmMac, IDatapath dstDatapath, int dstPort) {
		if (containsVm(vmMac)) {
			dstDatapath.connectVm(vmMac, dstPort);
			disconnectVm(vmMac);
		} else {
			throw new IllegalArgumentException("VM with such MAC (" + vmMac + ") not connected to datapath " + toString());
		}
	}

	// Listeners

	@Override
	public synchronized void registerListener(IDatapathListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public synchronized void unregisterListener(IDatapathListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(JSONObject[] commands, CommandType commandType) {
		if (listeners != null && !listeners.isEmpty()) {
			switch (commandType) {
				case FLOW_ADD:
					for (IDatapathListener listener : listeners) {
						listener.onConnection(commands);
					}
					break;
				case FLOW_REMOVE:
					for (IDatapathListener listener : listeners) {
						listener.onDisconnection(commands);
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown command type " + commandType);
			}
		}
	}


	/**
	 * Other
	 */

	private static JSONObject[] createCommands(IFirewallRule rule, CommandType commandType) {
		JSONObject[] commands;
		if (rule instanceof OnePortFirewallBroadcastRule ||
				rule instanceof OnePortFirewallSubnetRule) {
			commands = new JSONObject[1];
			if (commandType == CommandType.FLOW_ADD) {
				commands[0] = rule.ovsOutFlowAddCommand();
			} else if (commandType == CommandType.FLOW_REMOVE) {
				commands[0] = rule.ovsOutFlowRemoveCommand();
			} else {
				throw new IllegalArgumentException("Unknown command type " + commandType);
			}
		} else {
			commands = new JSONObject[2];
			if (commandType == CommandType.FLOW_ADD) {
				commands[0] = rule.ovsInFlowAddCommand();
				commands[1] = rule.ovsOutFlowAddCommand();
			} else if (commandType == CommandType.FLOW_REMOVE) {
				commands[0] = rule.ovsInFlowRemoveCommand();
				commands[1] = rule.ovsOutFlowRemoveCommand();
			} else {
				throw new IllegalArgumentException("Unknown command type " + commandType);
			}
		}
		return commands;
	}

	@Override
	public String toString() {
		return name + " (" + dpid + ")";
	}

}
