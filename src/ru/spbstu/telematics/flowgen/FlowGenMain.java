package ru.spbstu.telematics.flowgen;


import ru.spbstu.telematics.flowgen.openflow.Datapath;
import ru.spbstu.telematics.flowgen.openflow.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.IFirewallRule;

import java.util.List;

public class FlowGenMain {

	public static void main(String[] args) {

		String dpid = "00:00:b6:60:ff:e5:93:4f";
		String gwMac = "FF:FF:AA:AA:FF:FF";
		int trunkPort = 1;
		int firewallPort = 2;
		IDatapath dp = new Datapath(dpid,"ovs-network", trunkPort, firewallPort, gwMac);

		int vmPort = 2;
		dp.connectVm("00:00:00:00:00:11", ++vmPort);
		dp.connectVm("00:00:00:00:00:12", ++vmPort);
		dp.connectVm("00:00:00:00:00:13", ++vmPort);

		List<IFirewallRule> rules = dp.getAllRules();
		for (IFirewallRule rule : rules) {
			// TODO NPE here!
			System.out.println(rule.ovsInFlowAddCommand().toString());
			System.out.println(rule.ovsOutFlowAddCommand().toString());
			System.out.println(rule.ovsInFlowRemoveCommand().toString());
			System.out.println(rule.ovsOutFlowRemoveCommand().toString());
			System.out.println();
		}


		// TODO refactor Datapath
		// TODO Exceptions + LOG

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
