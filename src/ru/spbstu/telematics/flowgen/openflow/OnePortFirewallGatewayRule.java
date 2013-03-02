package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

/**
 * Rule for processing external network traffic forwarded by gateway.
 * This rule doesn't affects outgoing traffic to broadcast domain
 * in case of analyzing destination MACs of ethernet frames.
 * (As you know, outgoing frames for broadcast domain use MACs of hosts (not MAC of gateway) as frame's destination MAC)
 * To process traffic for broadcast domain use OnePortFirewallSubnetRule class.
 */

public class OnePortFirewallGatewayRule extends OnePortFirewallRule {

	private String gatewayMac;


	/**
	 * Constructors
	 */

	public OnePortFirewallGatewayRule(String dpid, boolean active, int inFlowPriority, int outFlowPriority,
								 int firewallPort, int gatewayPort, String gatewayMac) {
		super(dpid, active, inFlowPriority, outFlowPriority, firewallPort, gatewayPort);
		setGatewayMac(gatewayMac);
	}

	public OnePortFirewallGatewayRule(String dpid, int firewallPort, int gatewayPort, String gatewayMac) {
		this(dpid, true, OpenflowUtils.IN_TRUNK_FLOW_PRIORITY, OpenflowUtils.OUT_TRUNK_FLOW_PRIORITY,
				firewallPort, gatewayPort, gatewayMac);
	}

	/**
	 * Gateway MAC
	 */

	public String getGatewayMac() {
		return gatewayMac;
	}

	public void setGatewayMac(String mac) {
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong gateway MAC");
		}
		gatewayMac = mac.toLowerCase();
	}


	/**
	 * Gateway port
	 */

	public int getGatewayPort() {
		return getTargetPort();
	}

	public void setGatewayPort(int port) {
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
		sb.append(StringUtils.omitDelimiters(gatewayMac, OpenflowUtils.MAC_DELIMITER));
		return sb.toString();
	}

	@Override
	public String getInFlowName() {
		StringBuilder sb = new StringBuilder();
		sb.append(getRuleName());
		sb.append(NAME_DELIMITER);
		sb.append(FLOW_NAME_IN_LABEL);
		return sb.toString();
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
		JSONObject command = new JSONObject();
		try {
			command.put(FLOW_DPID,		getDpid());
			command.put(FLOW_NAME,		getInFlowName());
			command.put(FLOW_PRIORITY,	getInFlowPriority());
			command.put(FLOW_ACTIVITY,	isActive());
			command.put(FLOW_IN_PORT,	getTargetPort());
			command.put(FLOW_ACTIONS,	FLOW_OUT_PORTS_PREFIX + getFirewallPort());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return command;
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
			command.put(FLOW_DST_MAC,	getGatewayMac());
			command.put(FLOW_ACTIONS,	FLOW_OUT_PORTS_PREFIX + getTargetPort());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return command;
	}

	@Override
	public JSONObject ovsInFlowRemoveCommand() {
		JSONObject command = new JSONObject();
		try {
			command.put(FLOW_NAME,	getInFlowName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return command;
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
