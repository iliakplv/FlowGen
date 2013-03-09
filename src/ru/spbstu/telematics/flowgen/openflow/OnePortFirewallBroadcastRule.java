package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

/**
 * Rule for processing outgoing broadcast traffic.
 * This rule doesn't generate add/remove JSON commands (returns null) for incoming flow.
 * This flow already processed by other rules (OnePortFirewallGatewayRule and OnePortFirewallVmRule).
 * Use this rule to process broadcast traffic (with destination MAC = FF:FF:FF:FF:FF:FF, e.g. ARP requests)
 */

public class OnePortFirewallBroadcastRule extends OnePortFirewallRule {

	// Not used for generating JSON commands
	private static final int BROADCAST_TRUNK_PORT = OpenflowUtils.MAX_PORT;

	private static final String FLOW_NAME_BROADCAST_LABEL = "broadcast";
	private static final String BROADCAST_MAC = "ff:ff:ff:ff:ff:ff";
	private static final String BROADCAST_OUT_PORTS = "all";


	/**
	 * Constructors
	 */

	public OnePortFirewallBroadcastRule(String dpid, boolean active, int outFlowPriority, int firewallPort) {
		super(dpid, active, OpenflowUtils.IN_BROADCAST_FLOW_PRIORITY, outFlowPriority, firewallPort, BROADCAST_TRUNK_PORT);
	}

	public OnePortFirewallBroadcastRule(String dpid, int firewallPort) {
		this(dpid, true, OpenflowUtils.OUT_BROADCAST_FLOW_PRIORITY, firewallPort);
	}


	/**
	 * RULE
	 */

	@Override
	public String getRuleName() {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.omitDelimiters(getDpid(), OpenflowUtils.DPID_DELIMITER));
		sb.append(NAME_DELIMITER);
		sb.append(FLOW_NAME_BROADCAST_LABEL);
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
			command.put(FLOW_DST_MAC,	BROADCAST_MAC);
			command.put(FLOW_ACTIONS,	FLOW_OUT_PORTS_PREFIX + BROADCAST_OUT_PORTS);
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
