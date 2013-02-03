package ru.spbstu.telematics.flowgen.openflow;


import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public abstract class FirewallRule {

	private String mDpid;
	private int mInPort;


	public FirewallRule(String dpid, int inPort) {
		setDpid(dpid);
		setInPort(inPort);
	}


	/**
	 * DPID
	 *
	 */

	public String getDpid() {
		return mDpid;
	}

	public void setDpid(String dpid) {
		if (!OpenflowUtils.validateDpid(dpid)) {
			throw new IllegalArgumentException("Wrong DPID value");
		}
		mDpid = dpid;
	}


	/**
	 * Ingress Port
	 *
	 */

	public int getInPort() {
		return mInPort;
	}

	public void setInPort(int inPort) {
		if (!OpenflowUtils.validatePortNumber(inPort)) {
			throw new IllegalArgumentException("Wrong port number");
		}
		mInPort = inPort;
	}

}
