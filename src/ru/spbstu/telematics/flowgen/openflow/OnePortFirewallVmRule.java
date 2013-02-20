package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

public class OnePortFirewallVmRule extends OnePortFirewallRule {

	private String mVmMac;


	/**
	 * Constructors
	 */

	public OnePortFirewallVmRule(String dpid, boolean active, int inFlowPriority, int outFlowPriority,
								 int firewallPort, int targetPort, String vmMac) {
		super(dpid, active, inFlowPriority, outFlowPriority, firewallPort, targetPort);
		setVmMac(vmMac);
	}

	public OnePortFirewallVmRule(String dpid, int firewallPort, int targetPort, String vmMac) {
		this(dpid, true, OpenflowUtils.IN_FLOW_PRIORITY, OpenflowUtils.OUT_VM_FLOW_PRIORITY,
				firewallPort, targetPort, vmMac);
	}

	/**
	 * VM MAC
	 */

	public String getVmMac() {
		return mVmMac;
	}

	public void setVmMac(String mac) {
		if (!OpenflowUtils.validateMac(mac)) {
			throw new IllegalArgumentException("Wrong VM MAC");
		}
		mVmMac = mac.toLowerCase();
	}

	/**
	 * RULE
	 */

	@Override
	public String getRuleName() {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.omitDelimiters(getDpid(), OpenflowUtils.DPID_DELIMITER));
		sb.append(NAME_DELIMITER);
		sb.append(mVmMac);
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
			command.put(FLOW_DST_MAC,	getVmMac());
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