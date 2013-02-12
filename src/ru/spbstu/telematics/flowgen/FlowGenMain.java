package ru.spbstu.telematics.flowgen;


import ru.spbstu.telematics.flowgen.openflow.FirewallRule;
import ru.spbstu.telematics.flowgen.openflow.OneIfaceFirewallRule;

public class FlowGenMain {

	public static void main(String[] args) {

		FirewallRule rule = new OneIfaceFirewallRule("00:00:00:00:00:00:00:01", 1, 2, "12:34:65:78:9A:B0");

		System.out.println(rule.ovsInFlowAddCommand().toString());
		System.out.println(rule.ovsOutFlowAddCommand().toString());

		System.out.println(rule.ovsInFlowRemoveCommand().toString());
		System.out.println(rule.ovsOutFlowRemoveCommand().toString());


	}
}
