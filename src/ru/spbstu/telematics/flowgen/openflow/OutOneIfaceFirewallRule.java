package ru.spbstu.telematics.flowgen.openflow;


import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public class OutOneIfaceFirewallRule extends  OneIfaceFirewallRule {

	private String mVmMac;


	public OutOneIfaceFirewallRule(String dpid, int inPort, int firewallPort, String vmMac) {
		super(dpid, inPort, firewallPort);
		setVmMac(vmMac);
	}


	/**
	 * VM MAC Address
	 */

	public String getVmMac() {
		return mVmMac;
	}

	public void setVmMac(String vmMac) {
		if (!OpenflowUtils.validateMac(vmMac)) {
			throw new IllegalArgumentException("Wrong MAC address");
		}
		mVmMac = vmMac;
	}

}
