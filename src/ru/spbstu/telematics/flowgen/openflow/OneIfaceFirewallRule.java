package ru.spbstu.telematics.flowgen.openflow;


import java.util.Set;

public class OneIfaceFirewallRule extends FirewallRule {

	public OneIfaceFirewallRule(String dpid, int inPort, Set<Integer> outPorts) {
		super(dpid, inPort, outPorts);
	}
}
