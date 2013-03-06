package ru.spbstu.telematics.flowgen;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.openflow.Datapath;
import ru.spbstu.telematics.flowgen.openflow.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.IDatapathListener;
import ru.spbstu.telematics.flowgen.openflow.StaticFlowPusherClient;

import java.util.HashMap;
import java.util.Set;


public class FlowGenMain {

	// TODO LOG

	public static void main(String[] args) {

		fakeTest();

//		testVn0();

	}

	public static void testVn0() {

		// Datapath, Gateway

		String dpid = "00:00:a6:49:24:26:a5:40";
		String name = "qbr1dee26dc-b0";
		int trunkPort = 1;
		int firewallPort = 3;
		String gwMac = "fa:16:3e:15:2d:df";
		IDatapath datapath = new Datapath(dpid, name, trunkPort, firewallPort, gwMac);

		// SFP client

		StaticFlowPusherClient sfpClient = new StaticFlowPusherClient("localhost", 8080);
		datapath.registerListener(sfpClient);

		// VMs

		HashMap<Integer, String> vmPortMacMap = new HashMap<Integer, String>();
		vmPortMacMap.put(4, "fa:16:3e:69:ab:bf");
		vmPortMacMap.put(5, "fa:16:3e:38:0f:e9");


		// Adding flows

		datapath.connectGateway();
		datapath.connectSubnet();

		Set<Integer> ports = vmPortMacMap.keySet();
		for (int port : ports) {
			datapath.connectVm(vmPortMacMap.get(port), port);
		}


		// Removing flows
//
//		for (int port : ports) {
//			datapath.disconnectVm(port);
//		}
//
//		datapath.disconnectSubnet();
//		datapath.disconnectGateway();
	}

	public static void fakeTest() {
		// Datapath, Gateway

		String dpid = "00:00:00:00:00:00:00:FF";
		String name = "ololo";
		int trunkPort = 1;
		int firewallPort = 2;
		String gwMac = "00:00:00:00:00:FF";
		IDatapath datapath = new Datapath(dpid, name, trunkPort, firewallPort, gwMac);

		// SFP client

		datapath.registerListener(new IDatapathListener() {
			@Override
			public void onConnection(JSONObject[] commands) {
				for (JSONObject command : commands) {
					System.out.println(command.toString());
				}
			}
			@Override
			public void onDisconnection(JSONObject[] commands) {
				for (JSONObject command : commands) {
					System.out.println(command.toString());
				}
			}
		});

		// VMs

		HashMap<Integer, String> vmPortMacMap = new HashMap<Integer, String>();
		vmPortMacMap.put(3, "11:11:11:11:11:11");
		vmPortMacMap.put(4, "22:22:22:22:22:22");


		// Adding flows

		datapath.connectGateway();
		datapath.connectSubnet();

		Set<Integer> ports = vmPortMacMap.keySet();
		for (int port : ports) {
			datapath.connectVm(vmPortMacMap.get(port), port);
		}


		// Removing flows

		for (int port : ports) {
			datapath.disconnectVm(port);
		}

		datapath.disconnectSubnet();
		datapath.disconnectGateway();

	}

}
