package ru.spbstu.telematics.flowgen;


import ru.spbstu.telematics.flowgen.openflow.Datapath;
import ru.spbstu.telematics.flowgen.openflow.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.StaticFlowPusherClient;
import ru.spbstu.telematics.flowgen.utils.DatapathLogger;

import java.util.HashMap;
import java.util.Set;


public class FlowGenMain {

	public static void main(String[] args) {

		testVn0();

	}

	public static void testVn0() {

		// Datapath, Gateway

		String dpid = "00:00:a6:49:24:26:a5:40";
		String name = "qbr1dee26dc-b0";
		int trunkPort = 1;
		int firewallPort = 3;
		String gwMac = "fa:16:3e:15:2d:df";
		Datapath datapath = new Datapath(dpid, name, trunkPort, firewallPort, gwMac);
		datapath.registerListener(new DatapathLogger(datapath.getDpid(), datapath.getName()));

		// SFP client

		StaticFlowPusherClient sfpClient = new StaticFlowPusherClient("127.0.0.1", 8080);

		// VMs

		HashMap<Integer, String> vmPortMacMap = new HashMap<Integer, String>();
		vmPortMacMap.put(4, "fa:16:3e:69:ab:bf");
		vmPortMacMap.put(5, "fa:16:3e:38:0f:e9");
		Set<Integer> ports = vmPortMacMap.keySet();



		// Adding flows

//		REGISTER TO ADD
		datapath.registerListener(sfpClient);

		datapath.connectGateway();
		datapath.connectBroadcast();
		datapath.connectSubnet();
		for (int port : ports) {
			datapath.connectVm(vmPortMacMap.get(port), port);
		}

//		UNREGISTER TO KEEP
		datapath.unregisterListener(sfpClient);



		// Removing flows

//		REGISTER TO REMOVE
//		datapath.registerListener(sfpClient);

		for (int port : ports) {
			datapath.disconnectVm(port);
		}
		datapath.disconnectSubnet();
		datapath.disconnectBroadcast();
		datapath.disconnectGateway();
	}

}
