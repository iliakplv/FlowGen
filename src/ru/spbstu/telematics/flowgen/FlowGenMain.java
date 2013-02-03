package ru.spbstu.telematics.flowgen;


import ru.spbstu.telematics.flowgen.openflow.InOneIfaceFirewallRule;
import ru.spbstu.telematics.flowgen.openflow.OutOneIfaceFirewallRule;


public class FlowGenMain {

	public static void main(String[] args) {

		InOneIfaceFirewallRule inRule = new InOneIfaceFirewallRule("00:00:82:39:d8:7f:90:4c", 2, 1);

		OutOneIfaceFirewallRule outRule = new OutOneIfaceFirewallRule("00:00:82:39:d8:7f:90:4c", 2, 1, "08:00:27:69:82:bc");


	}
}
