package ru.spbstu.telematics.flowgen;


import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.openflow.Datapath;
import ru.spbstu.telematics.flowgen.openflow.IDatapath;

import java.util.List;

public class FlowGenMain {

	public static void main(String[] args) {

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

		// TODO LOG

//		String sfpUrl =	"http://192.168.168.24:8080/wm/staticflowentrypusher/json";
//		String dpid = "00:00:b6:60:ff:e5:93:4f";
//		int fwPort = 1;
//
//		int[] vmPorts = new int[]	{2};
//		String[] vmMacs = 			{"12:34:65:78:9A:B0"};
//
//		HttpClient httpClient = new DefaultHttpClient();
//		try {
//			for (int i = 0; i < vmPorts.length; i++) {
//				OnePortFirewallRule rule = new OnePortFirewallRule(dpid, fwPort, vmPorts[i], vmMacs[i]);
//
//				HttpPost inRequest = new HttpPost(sfpUrl);
//				inRequest.addHeader("content-type", "application/x-www-form-urlencoded");
//				StringEntity inParams = new StringEntity(rule.ovsInFlowAddCommand().toString());
//				inRequest.setEntity(inParams);
//				httpClient.execute(inRequest);
//
//				HttpPost outRequest = new HttpPost(sfpUrl);
//				inRequest.addHeader("content-type", "application/x-www-form-urlencoded");
//				StringEntity outParams = new StringEntity(rule.ovsOutFlowAddCommand().toString());
//				outRequest.setEntity(outParams);
//				httpClient.execute(outRequest);
//			}
//
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			httpClient.getConnectionManager().shutdown();
//		}

	}

}
