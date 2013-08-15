package ru.spbstu.telematics.flowgen.openflow.datapath;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.openflow.rules.Command;
import ru.spbstu.telematics.flowgen.openflow.rules.IFirewallRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallBroadcastRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallGatewayRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallHostRule;
import ru.spbstu.telematics.flowgen.openflow.rules.OnePortFirewallSubnetRule;
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
	private boolean connectedToSubnet = false;
	private String gatewayMac = null;
	private int trunkPort = OpenflowUtils.DEFAULT_PORT;

	// VM
	private Map<String, Integer> macPortMap;
	private Map<String, Integer> macPriorityMap;
	private Map<Integer, Integer> portHostsMap;

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
		macPriorityMap = new HashMap<String, Integer>();
		portHostsMap = new HashMap<Integer, Integer>();
		listeners = new LinkedList<IDatapathListener>();
	}


	/**
	 * DPID
	 */

	@Override
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

	@Override
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
		return OpenflowUtils.macEquals(gatewayMac, mac);
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
	public boolean containsHost(String mac) {
		return macPortMap.containsKey(mac.toLowerCase());
	}

	@Override
	public int getHostPort(String mac) {
		int result;

		if (containsHost(mac)) {
			result = macPortMap.get(mac.toLowerCase());
		} else {
			result = OpenflowUtils.DEFAULT_PORT;
		}

		return result;
	}

	@Override
	public Map<String, Integer> getMacPortMap() {
		return new HashMap<String, Integer>(macPortMap);
	}

	@Override
	public int getHostPriority(String mac) {
		int result;

		if (containsHost(mac)) {
			result = macPriorityMap.get(mac.toLowerCase());
		} else {
			result = OpenflowUtils.HOST_FLOW_PRIORITY;
		}

		return result;
	}

	@Override
	public Map<String, Integer> getMacPriorityMap() {
		return new HashMap<String, Integer>(macPriorityMap);
	}


	/**
	 * IDatapath implementation
	 */

	@Override
	public synchronized void connectHost(String mac, int port, int flowPriority) {

		if (mac == null) {
			throw new IllegalArgumentException("VM MAC is null in datapath " + toString());
		}
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong VM MAC (" + mac + ") in datapath " + toString());
		}
		if (isGatewayMac(mac)) {
			throw new IllegalArgumentException("New VM MAC equals to gateway MAC (" + gatewayMac + ") of datapath " + toString());
		}
		if (containsHost(mac)) {
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

		if (!OpenflowUtils.validatePriority(flowPriority)) {
			throw new IllegalArgumentException("Wrong flow priority value (" + flowPriority + ") in datapath " + toString());
		}

		Command.Type commandType;
		if (isPortEmpty(port)) {
			commandType = Command.Type.FlowAddFirstVm;
		} else {
			commandType = Command.Type.FlowAddAnotherVm;
		}
		mac = mac.toLowerCase();
		macPortMap.put(mac, port);
		macPriorityMap.put(mac, flowPriority);
		incrementNumberOfVmsConnectedToPort(port);
		IFirewallRule rule = getHostRule(mac);
		JSONObject[] commands = createCommands(rule, commandType);
		notifyListeners(commands, Command.Action.FlowAdd);
	}

	@Override
	public void connectHost(String mac, int port) {
		connectHost(mac, port, OpenflowUtils.HOST_FLOW_PRIORITY);
	}

	@Override
	public synchronized void disconnectHost(String mac) {

		if(!containsHost(mac)) {
			throw new IllegalArgumentException("VM with such MAC (" + mac + ") not connected to datapath " + toString());
		}

		int port = getHostPort(mac);
		Command.Type commandType;
		if (isLastVmConnectedToPort(port)) {
			commandType = Command.Type.FlowRemoveLastVm;
		} else {
			commandType = Command.Type.FlowRemoveAnotherVm;
		}
		mac = mac.toLowerCase();
		IFirewallRule rule = getHostRule(mac);
		macPortMap.remove(mac);
		macPriorityMap.remove(mac);
		decrementNumberOfVmsConnectedToPort(port);
		JSONObject[] commands = createCommands(rule, commandType);
		notifyListeners(commands, Command.Action.FlowRemove);
	}

	@Override
	public IFirewallRule getHostRule(String mac) {
		mac = mac.toLowerCase();
		if (containsHost(mac)) {
			int priority = macPriorityMap.get(mac);
			return new OnePortFirewallHostRule(dpid, true, priority, priority, firewallPort, macPortMap.get(mac), mac);
		}
		return null;
	}

	@Override
	public List<IFirewallRule> getAllHostsRules() {
		ArrayList<IFirewallRule> rules = new ArrayList<IFirewallRule>();
		Set<String> macs = macPortMap.keySet();
		for (String mac : macs) {
			rules.add(getHostRule(mac));
		}
		return rules;
	}

	// Network

	private void connectGateway() {
		JSONObject[] commands = createCommands(getGatewayRule(), Command.Type.FlowAddGateway);
		notifyListeners(commands, Command.Action.FlowAdd);
	}

	private void disconnectGateway() {
		JSONObject[] commands = createCommands(getGatewayRule(), Command.Type.FlowRemoveGateway);
		notifyListeners(commands, Command.Action.FlowRemove);
	}

	private IFirewallRule getGatewayRule() {
		return new OnePortFirewallGatewayRule(dpid, firewallPort, trunkPort, gatewayMac);
	}

	private void connectBroadcast() {
		JSONObject[] commands = createCommands(getBroadcastRule(), Command.Type.FlowAddBroadcast);
		notifyListeners(commands, Command.Action.FlowAdd);
	}

	private void disconnectBroadcast() {
		JSONObject[] commands = createCommands(getBroadcastRule(), Command.Type.FlowRemoveBroadcast);
		notifyListeners(commands, Command.Action.FlowRemove);
	}

	private IFirewallRule getBroadcastRule() {
		return new OnePortFirewallBroadcastRule(dpid, firewallPort);
	}

	private void connectSubnet() {
		JSONObject[] commands = createCommands(getSubnetRule(), Command.Type.FlowAddSubnet);
		notifyListeners(commands, Command.Action.FlowAdd);
	}

	private void disconnectSubnet() {
		JSONObject[] commands = createCommands(getSubnetRule(), Command.Type.FlowRemoveSubnet);
		notifyListeners(commands, Command.Action.FlowRemove);
	}

	private IFirewallRule getSubnetRule() {
		return new OnePortFirewallSubnetRule(dpid, firewallPort, trunkPort);
	}

	@Override
	public synchronized void connectToNetwork(boolean connectSubnet) {
		if (!connectedToNetwork) {
			// order!
			connectBroadcast();
			if (!connectedToSubnet && connectSubnet) {
				connectSubnet();
				connectedToSubnet = true;
			}
			connectGateway();
			connectedToNetwork = true;
		}
	}

	@Override
	public synchronized void disconnectFromNetwork() {
		if (connectedToNetwork) {
			// order!
			connectedToNetwork = false;
			disconnectGateway();
			if (connectedToSubnet) {
				connectedToSubnet = false;
				disconnectSubnet();
			}
			disconnectBroadcast();
		}
	}

	@Override
	public boolean isConnectedToNetwork() {
		return connectedToNetwork;
	}

	@Override
	public boolean isConnectedToSubnet() {
		return connectedToNetwork && connectedToSubnet;
	}

	@Override
	public List<IFirewallRule> getAllNetworkRules() {
		List<IFirewallRule> rules = new ArrayList<IFirewallRule>();
		if (connectedToNetwork) {
			rules.add(getBroadcastRule());
			if (connectedToSubnet) {
				rules.add(getSubnetRule());
			}
			rules.add(getGatewayRule());
		}
		return rules;
	}

	@Override
	public List<IFirewallRule> getAllRules() {
		List<IFirewallRule> rules = getAllNetworkRules();
		List<IFirewallRule> vmRules = getAllHostsRules();
		rules.addAll(vmRules);
		return rules;
	}

	// VM Migration

	@Override
	public void migrateVm(String vmMac, IDatapath dstDatapath, int dstPort) {
		// TODO must be safe
		if (containsHost(vmMac)) {
			dstDatapath.connectHost(vmMac, dstPort);
			disconnectHost(vmMac);
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

	private void notifyListeners(JSONObject[] commands, Command.Action action) {
		if (listeners != null && !listeners.isEmpty()) {
			switch (action) {
				case FlowAdd:
					for (IDatapathListener listener : listeners) {
						listener.onConnection(commands);
					}
					break;
				case FlowRemove:
					for (IDatapathListener listener : listeners) {
						listener.onDisconnection(commands);
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown command action type " + action);
			}
		}
	}


	/**
	 * Other
	 */

	private int numberOfVmsConnectedToPort(int port) {
		if (!OpenflowUtils.validatePortNumber(port)) {
			throw new IllegalArgumentException("Wrong port number (" + port + ") checked in datapath " + toString());
		}
		if (port == trunkPort) {
			throw new IllegalArgumentException("Checked VM port equals to trunk port (" + trunkPort + ") of datapath " + toString());
		}
		if (port == firewallPort) {
			throw new IllegalArgumentException("Checked VM port equals to firewall port (" + firewallPort + ") of datapath " + toString());
		}

		int result = 0;
		if (portHostsMap.containsKey(port)) {
			result = portHostsMap.get(port);
		}
		return result;
	}

	private int incrementNumberOfVmsConnectedToPort(int port) {
		int number = numberOfVmsConnectedToPort(port);
		number++;
		portHostsMap.put(port, number);
		return number;
	}

	private int decrementNumberOfVmsConnectedToPort(int port) {
		int number = numberOfVmsConnectedToPort(port);
		if (number == 0) {
			throw new IllegalArgumentException("Port " + port + " of datapath " + toString() + " already empty");
		} else {
			number--;
		}
		portHostsMap.put(port, number);
		return number;
	}

	private boolean isPortEmpty(int port) {
		return numberOfVmsConnectedToPort(port) == 0;
	}

	private boolean isLastVmConnectedToPort(int port) {
		return numberOfVmsConnectedToPort(port) == 1;
	}


	private static JSONObject[] createCommands(IFirewallRule rule, Command.Type commandType) {

		Command.RuleGroup ruleGroup = Command.getRuleGroup(commandType);

		// For BROADCAST or SUBNET rule only OUT flow needed.
		// If new VM is connected to port which had one or more other connected VM or
		// if VM is disconnected from port which had one or more other connected VM only OUT flow needed.
		// For other cases need to create 2 flows: IN and OUT.

		boolean anotherVm = commandType == Command.Type.FlowAddAnotherVm ||
				commandType == Command.Type.FlowRemoveAnotherVm;

		boolean onlyOutFlow = ruleGroup == Command.RuleGroup.RuleBroadcast ||
				ruleGroup == Command.RuleGroup.RuleSubnet ||
				(ruleGroup == Command.RuleGroup.RuleVm && anotherVm);

		JSONObject[] commands;
		Command.Action action = Command.getAction(commandType);

		if (onlyOutFlow) {

			commands = new JSONObject[1];
			if (action == Command.Action.FlowAdd) {
				commands[0] = rule.ovsOutFlowAddCommand();
			} else if (action == Command.Action.FlowRemove) {
				commands[0] = rule.ovsOutFlowRemoveCommand();
			} else {
				throw new IllegalArgumentException("Unknown command action of " + commandType);
			}

		} else {

			commands = new JSONObject[2];
			// OUT flow first!
			if (action == Command.Action.FlowAdd) {
				commands[0] = rule.ovsOutFlowAddCommand();
				commands[1] = rule.ovsInFlowAddCommand();
			} else if (action == Command.Action.FlowRemove) {
				commands[0] = rule.ovsOutFlowRemoveCommand();
				commands[1] = rule.ovsInFlowRemoveCommand();
			} else {
				throw new IllegalArgumentException("Unknown command action of " + commandType);
			}

		}

		return commands;
	}

	@Override
	public String toString() {
		return name + " (" + dpid + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Datapath datapath = (Datapath) o;

		return dpid.equals(datapath.dpid);

	}

	@Override
	public int hashCode() {
		return dpid.hashCode();
	}
}
