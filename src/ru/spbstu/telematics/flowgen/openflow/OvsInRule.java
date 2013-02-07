package ru.spbstu.telematics.flowgen.openflow;


import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

public class OvsInRule extends InOneIfaceFirewallRule {

	private String mDatapathName;
	private int mPriority;
	private boolean mActive;


	public OvsInRule(String dpid, String datapathName, int priority,
					 boolean active, int inPort, int firewallPort) {
		super(dpid, inPort, firewallPort);
		setDatapathName(datapathName);
		setPriority(priority);
		setActive(active);
	}


	/**
	 * Datapath
	 */

	public String getDatapathName() {
		return mDatapathName;
	}

	public void setDatapathName(String datapathName) {
		if (StringUtils.isNullOrEmpty(datapathName)) {
			throw new IllegalArgumentException("Wrong datapath name");
		}
		mDatapathName = datapathName;
	}


	/**
	 * Priority
	 */

	public int getPriority() {
		return mPriority;
	}

	public void setPriority(int priority) {
		if (!OpenflowUtils.validatePriority(priority)) {
			throw new IllegalArgumentException("Wrong priority value");
		}
		mPriority = priority;
	}


	/**
	 * Active
	 */

	public boolean isActive() {
		return mActive;
	}

	public void setActive(boolean active) {
		mActive = active;
	}
}
