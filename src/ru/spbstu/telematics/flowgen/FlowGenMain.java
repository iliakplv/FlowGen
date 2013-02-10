package ru.spbstu.telematics.flowgen;


import ru.spbstu.telematics.flowgen.openflow.OneIfaceFirewallRule;

public class FlowGenMain {

	public static void main(String[] args) {

		OneIfaceFirewallRule rule = new OneIfaceFirewallRule("00:00:00:00:00:00:00:01", 1, 2, "12:34:65:78:9A:B0");

		System.out.println(rule.getFlowName() + '\n' + rule.toString());


	}
}
