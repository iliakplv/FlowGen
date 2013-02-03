package ru.spbstu.telematics.flowgen.openflow;


import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public abstract class OneIfaceFirewallRule extends FirewallRule {

	private int mFirewallPort;


	public OneIfaceFirewallRule(String dpid, int inPort, int firewallPort) {
		super(dpid, inPort);
		setFirewallPort(firewallPort);
	}


	/**
	 * Firewall Port
	 *
	 */

	public int getFirewallPort() {
		return mFirewallPort;
	}

	public void setFirewallPort(int firewallPort) {
		if (!OpenflowUtils.validatePortNumber(firewallPort)) {
			throw new IllegalArgumentException("Wrong port number");
		}
		mFirewallPort = firewallPort;
	}
}
