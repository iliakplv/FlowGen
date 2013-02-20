package ru.spbstu.telematics.flowgen;


import ru.spbstu.telematics.flowgen.openflow.IFirewallRule;
import ru.spbstu.telematics.flowgen.openflow.OnePortFirewallGatewayRule;
import ru.spbstu.telematics.flowgen.openflow.OnePortFirewallSubnetRule;
import ru.spbstu.telematics.flowgen.openflow.OnePortFirewallVmRule;

public class FlowGenMain {

	public static void main(String[] args) {

		String dpid = "00:00:b6:60:ff:e5:93:4f";
		String gwMac = "FF:FF:FF:FF:FF:AA";
		String vmMac = "00:00:00:00:00:11";
		int trunkPort = 1;
		int firewallPort = 2;
		int vmPort = 3;

		IFirewallRule vm = new OnePortFirewallVmRule(dpid, firewallPort, vmPort, vmMac);
		IFirewallRule gw = new OnePortFirewallGatewayRule(dpid, firewallPort, trunkPort, gwMac);
		IFirewallRule sn = new OnePortFirewallSubnetRule("00:00:b6:60:ff:e5:93:4f", firewallPort, trunkPort);

		System.out.println(vm.ovsInFlowAddCommand().toString());
		System.out.println(vm.ovsOutFlowAddCommand().toString());
		System.out.println(vm.ovsInFlowRemoveCommand().toString());
		System.out.println(vm.ovsOutFlowRemoveCommand().toString());
		System.out.println();
		System.out.println(gw.ovsInFlowAddCommand().toString());
		System.out.println(gw.ovsOutFlowAddCommand().toString());
		System.out.println(gw.ovsInFlowRemoveCommand().toString());
		System.out.println(gw.ovsOutFlowRemoveCommand().toString());
		System.out.println();
		System.out.println(sn.ovsInFlowAddCommand());
		System.out.println(sn.ovsOutFlowAddCommand().toString());
		System.out.println(sn.ovsInFlowRemoveCommand());
		System.out.println(sn.ovsOutFlowRemoveCommand().toString());


		// TODO refactor Datapath
		// TODO write tests for Datapath
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
