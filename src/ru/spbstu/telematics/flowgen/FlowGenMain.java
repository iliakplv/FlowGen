package ru.spbstu.telematics.flowgen;


import ru.spbstu.telematics.flowgen.openflow.InOneIfaceFirewallRule;
import ru.spbstu.telematics.flowgen.openflow.OutOneIfaceFirewallRule;
import ru.spbstu.telematics.flowgen.openflow.ovs.OvsInRule;
import ru.spbstu.telematics.flowgen.openflow.ovs.OvsOutRule;
import ru.spbstu.telematics.flowgen.utils.OpenflowUtils;


public class FlowGenMain {

	public static void main(String[] args) {

		InOneIfaceFirewallRule inRule = new InOneIfaceFirewallRule("00:00:82:39:d8:7f:90:4c", 2, 1);

		OutOneIfaceFirewallRule outRule = new OutOneIfaceFirewallRule("00:00:82:39:d8:7f:90:4c", 2, 1, "08:00:27:69:82:bc");

		OvsInRule ovsInRule1 = new OvsInRule("00:00:82:39:d8:7f:90:4c", "xapi0",
				OpenflowUtils.DEFAULT_RULE_PRIORITY, true, 2, 1);

		OvsOutRule ovsOutRule1 = new OvsOutRule("00:00:82:39:d8:7f:90:4c", "xapi0",
				OpenflowUtils.DEFAULT_RULE_PRIORITY, true, 2, 1, "08:00:27:69:82:bc");


	}
}
