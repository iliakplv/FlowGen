package ru.spbstu.telematics.flowgen.openflow.rules;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public abstract class OnePortFirewallRule implements IFirewallRule {

	protected static final char NAME_DELIMITER = '-';
	protected static final String FLOW_NAME_IN_LABEL = "i";
	protected static final String FLOW_NAME_OUT_LABEL = "o";

	protected static final String FLOW_DPID = 				"switch";
	protected static final String FLOW_NAME = 				"name";
	protected static final String FLOW_PRIORITY = 			"priority";
	protected static final String FLOW_ACTIVITY =			"active";
	protected static final String FLOW_IN_PORT = 			"ingress-port";
	protected static final String FLOW_DST_MAC = 			"dst-mac";
	protected static final String FLOW_ACTIONS = 			"actions";
	protected static final String FLOW_OUT_PORTS_PREFIX =	"output=";

	private String dpid;
	private boolean active;
	private int inFlowPriority;
	private int outFlowPriority;
	private int firewallPort = -1;
	private int targetPort = -1;


	/**
	 * Constructors
	 */

	public OnePortFirewallRule(String dpid, boolean active, int inFlowPriority, int outFlowPriority,
							   int firewallPort, int targetPort) {
		this(dpid, active, outFlowPriority, firewallPort, targetPort);
		setInFlowPriority(inFlowPriority);
	}

	public OnePortFirewallRule(String dpid, boolean active, int outFlowPriority,
							   int firewallPort, int targetPort) {
		this(dpid, active, outFlowPriority, firewallPort);
		setTargetPort(targetPort);
	}

	public OnePortFirewallRule(String dpid, boolean active, int outFlowPriority, int firewallPort) {
		setDpid(dpid);
		setActive(active);
		setOutFlowPriority(outFlowPriority);
		setFirewallPort(firewallPort);
	}


	/**
	 * DPID
	 */

	public String getDpid() {
		return dpid;
	}

	public void setDpid(String dpid) {
		if (!OpenflowUtils.validateDpid(dpid)) {
			throw new IllegalArgumentException("Wrong DPID value");
		}
		this.dpid = dpid.toLowerCase();
	}


	/**
	 * Activity
	 */

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}


	/**
	 * Priority
	 */

	public int getInFlowPriority() {
		return inFlowPriority;
	}

	public void setInFlowPriority(int priority) {
		if (!OpenflowUtils.validatePriority(priority)) {
			throw new IllegalArgumentException("Wrong in flow priority value");
		}
		inFlowPriority = priority;
	}

	public int getOutFlowPriority() {
		return outFlowPriority;
	}

	public void setOutFlowPriority(int priority) {
		if (!OpenflowUtils.validatePriority(priority)) {
			throw new IllegalArgumentException("Wrong out flow priority value");
		}
		outFlowPriority = priority;
	}


	/**
	 * Firewall port
	 */

	public int getFirewallPort() {
		return firewallPort;
	}

	public void setFirewallPort(int port) {
		if (!OpenflowUtils.validatePortNumber(port)) {
			throw new IllegalArgumentException("Wrong firewall port");
		}
		if (port == targetPort) {
			throw new IllegalArgumentException("New firewall port equals to current target port");
		}
		firewallPort = port;
	}

	/**
	 * Target port
	 */

	public int getTargetPort() {
		return targetPort;
	}

	public void setTargetPort(int port) {
		if (!OpenflowUtils.validatePortNumber(port)) {
			throw new IllegalArgumentException("Wrong target port");
		}
		if (port == firewallPort) {
			throw new IllegalArgumentException("New target port equals to current firewall port");
		}
		targetPort = port;
	}


	/**
	 * Names
	 */

	public abstract String getRuleName();

	public abstract String getInFlowName();

	public abstract String getOutFlowName();


	/**
	 * IFirewallRule Interface
	 */

	@Override
	public abstract JSONObject ovsInFlowAddCommand();

	@Override
	public abstract JSONObject ovsOutFlowAddCommand();

	@Override
	public abstract JSONObject ovsInFlowRemoveCommand();

	@Override
	public abstract JSONObject ovsOutFlowRemoveCommand();

}
