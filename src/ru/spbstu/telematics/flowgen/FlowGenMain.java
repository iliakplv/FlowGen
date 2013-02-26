package ru.spbstu.telematics.flowgen;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.openflow.Datapath;
import ru.spbstu.telematics.flowgen.openflow.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.IStaticFlowPusherClient;
import ru.spbstu.telematics.flowgen.openflow.StaticFlowPusherClient;


public class FlowGenMain {

	// TODO LOG

	public static void main(String[] args) {

		IStaticFlowPusherClient sfpClient = new StaticFlowPusherClient("127.0.0.1", 8080);
		System.out.println("\nSFP at " + ((StaticFlowPusherClient)sfpClient).getStaticFlowPusherUrl() + "\n");

		String dpid = "00:00:b6:60:ff:e5:93:4f";
		String gwMac = "FF:FF:AA:AA:FF:FF";
		int trunkPort = 1;
		int firewallPort = 2;
		IDatapath dp = new Datapath(dpid,"ovs-network", trunkPort, firewallPort, gwMac);

		int vmPort = 3;
		int vmMacLastByte = 13;


		System.out.println(" ===== Connect all to " + dp.toString() + " =====\n");

		for (int i = 0; i < 3; i++) {
			JSONObject[] commands = dp.connectVm("00:00:00:00:00:" + vmMacLastByte++, vmPort++);
			for (int j = 0; j < commands.length; j++) {
				System.out.println(commands[j].toString());
			}
			System.out.println();
		}

		JSONObject[] gwCommands = dp.connectGateway();
		for (int i = 0; i < gwCommands.length; i++) {
			System.out.println(gwCommands[i].toString());
		}
		System.out.println();

		JSONObject[] snCommands = dp.connectSubnet();
		for (int i = 0; i < snCommands.length; i++) {
			System.out.println(snCommands[i].toString());
		}


		System.out.println("\n ===== Disconnect all from " + dp.toString() + " =====\n");

		for (int i = 0; i < 3; i++) {
//			JSONObject[] commands = dp.disconnectVm("00:00:00:00:00:" + --vmMacLastByte);
			JSONObject[] commands = dp.disconnectVm(--vmPort);
			for (int j = 0; j < commands.length; j++) {
				System.out.println(commands[j].toString());
			}
			System.out.println();
		}

		gwCommands = dp.disconnectGateway();
		for (int i = 0; i < gwCommands.length; i++) {
			System.out.println(gwCommands[i].toString());
		}
		System.out.println();

		snCommands = dp.disconnectSubnet();
		for (int i = 0; i < snCommands.length; i++) {
			System.out.println(snCommands[i].toString());
		}

	}

}
