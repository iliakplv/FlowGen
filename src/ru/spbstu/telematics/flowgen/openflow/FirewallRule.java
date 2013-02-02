package ru.spbstu.telematics.flowgen.openflow;


import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

import java.util.Set;

public abstract class FirewallRule {

	private String mDpid;
	private int mInPort;
	private Set<Integer> mOutPorts;


	public FirewallRule(String dpid, int inPort, Set<Integer> outPorts) {
		setDpid(dpid);
		setInPort(inPort);
		setOutPorts(outPorts);
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

	public String getInPortString() {
		return Integer.valueOf(mInPort).toString();
	}

	public void setInPort(int inPort) {
		if (!OpenflowUtils.validatePortNumber(inPort)) {
			throw new IllegalArgumentException("Wrong port number");
		}
		mInPort = inPort;
	}


	/**
	 * Out Port
	 *
	 */

	public Set<Integer> getOutPorts() {
		return mOutPorts;
	}

	public String getOutPortsString() {
		Object[] ports = mOutPorts.toArray();
		String[] portsStrings = new String[ports.length];

		for (int i = 0; i < ports.length; i++) {
			portsStrings[i] = ports[i].toString();
		}

		return StringUtils.buildFromParts(portsStrings, OpenflowUtils.PORTS_DELIMITER);
	}

	public void setOutPorts(Set<Integer> outPorts) {
		if (!OpenflowUtils.validatePortsSet(outPorts)) {
			throw new IllegalArgumentException("Wrong ports set");
		}
		mOutPorts = outPorts;
	}

}
