package ru.spbstu.telematics.flowgen.openflow;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public abstract class OnePortFirewallRule implements FirewallRule {

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

	private String mDpid;
	private boolean mActive;
	private int mInFlowPriority;
	private int mOutFlowPriority;
	private int mFirewallPort	= -1;
	private int mTargetPort		= -1;


	/**
	 * Constructors
	 */

	public OnePortFirewallRule(String dpid, boolean active, int inFlowPriority, int outFlowPriority,
							   int firewallPort, int targetPort) {
		setDpid(dpid);
		setActive(active);
		setInFlowPriority(inFlowPriority);
		setOutFlowPriority(outFlowPriority);
		setFirewallPort(firewallPort);
		setTargetPort(targetPort);
	}


	/**
	 * DPID
	 */

	public String getDpid() {
		return mDpid;
	}

	public void setDpid(String dpid) {
		if (!OpenflowUtils.validateDpid(dpid)) {
			throw new IllegalArgumentException("Wrong DPID value");
		}
		mDpid = dpid.toLowerCase();
	}


	/**
	 * Activity
	 */

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean active) {
		mActive = active;
	}


	/**
	 * Priority
	 */

	public int getInFlowPriority() {
		return mInFlowPriority;
	}

	public void setInFlowPriority(int priority) {
		if (!OpenflowUtils.validatePriority(priority)) {
			throw new IllegalArgumentException("Wrong in flow priority value");
		}
		mInFlowPriority = priority;
	}

	public int getOutFlowPriority() {
		return mOutFlowPriority;
	}

	public void setOutFlowPriority(int priority) {
		if (!OpenflowUtils.validatePriority(priority)) {
			throw new IllegalArgumentException("Wrong out flow priority value");
		}
		mOutFlowPriority = priority;
	}


	/**
	 * Firewall port
	 */

	public int getFirewallPort() {
		return mFirewallPort;
	}

	public void setFirewallPort(int port) {
		if (!OpenflowUtils.validatePortNumber(port)) {
			throw new IllegalArgumentException("Wrong firewall port");
		}
		if (port == mTargetPort) {
			throw new IllegalArgumentException("New firewall port equals to current target port");
		}
		mFirewallPort = port;
	}

	/**
	 * Target port
	 */

	public int getTargetPort() {
		return mTargetPort;
	}

	public void setTargetPort(int port) {
		if (!OpenflowUtils.validatePortNumber(port)) {
			throw new IllegalArgumentException("Wrong target port");
		}
		if (port == mFirewallPort) {
			throw new IllegalArgumentException("New target port equals to current firewall port");
		}
		mTargetPort = port;
	}


	/**
	 * Names
	 */

	public abstract String getRuleName();

	public abstract String getInFlowName();

	public abstract String getOutFlowName();


	/**
	 * FirewallRule Interface
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
