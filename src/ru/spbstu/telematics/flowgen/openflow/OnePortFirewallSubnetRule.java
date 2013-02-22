package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

/**
 * Default rule for processing outgoing broadcast domain traffic.
 * This rule doesn't generate add/remove JSON commands (returns null) for incoming flow.
 * This flow already processed by OnePortFirewallGatewayRule.
 * Use this rule only if you want VM's to be able to communicate with other hosts in broadcast domain.
 */

public class OnePortFirewallSubnetRule extends OnePortFirewallRule {

	// Not used for generating JSON commands
	private static final int IN_SUBNET_FLOW_PRIORITY = OpenflowUtils.MIN_FLOW_PRIORITY;

	private static final String FLOW_NAME_SUBNET_LABEL = "subnet";

	/**
	 * Constructors
	 */

	public OnePortFirewallSubnetRule(String dpid, boolean active, int outFlowPriority,
									 int firewallPort, int trunkPort) {
		super(dpid, active, IN_SUBNET_FLOW_PRIORITY, outFlowPriority, firewallPort, trunkPort);
	}

	public OnePortFirewallSubnetRule(String dpid, int firewallPort, int trunkPort) {
		this(dpid, true, OpenflowUtils.OUT_SUBNET_FLOW_PRIORITY, firewallPort, trunkPort);
	}


	/**
	 * Trunk port
	 */

	public int getTrunkPort() {
		return getTargetPort();
	}

	public void setTrunkPort(int port) {
		setTargetPort(port);
	}


	/**
	 * RULE
	 */

	@Override
	public String getRuleName() {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.omitDelimiters(getDpid(), OpenflowUtils.DPID_DELIMITER));
		sb.append(NAME_DELIMITER);
		sb.append(FLOW_NAME_SUBNET_LABEL);
		return sb.toString();
	}

	@Override
	public String getInFlowName() {
		return null;
	}

	@Override
	public String getOutFlowName() {
		StringBuilder sb = new StringBuilder();
		sb.append(getRuleName());
		sb.append(NAME_DELIMITER);
		sb.append(FLOW_NAME_OUT_LABEL);
		return sb.toString();
	}

	@Override
	public JSONObject ovsInFlowAddCommand() {
		return null;
	}

	@Override
	public JSONObject ovsOutFlowAddCommand() {
		JSONObject command = new JSONObject();
		try {
			command.put(FLOW_DPID,		getDpid());
			command.put(FLOW_NAME,		getOutFlowName());
			command.put(FLOW_PRIORITY,	getOutFlowPriority());
			command.put(FLOW_ACTIVITY,	isActive());
			command.put(FLOW_IN_PORT,	getFirewallPort());
			command.put(FLOW_ACTIONS,	FLOW_OUT_PORTS_PREFIX + getTargetPort());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return command;
	}

	@Override
	public JSONObject ovsInFlowRemoveCommand() {
		return null;
	}

	@Override
	public JSONObject ovsOutFlowRemoveCommand() {
		JSONObject command = new JSONObject();
		try {
			command.put(FLOW_NAME,	getOutFlowName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return command;
	}
}
