package ru.spbstu.telematics.flowgen;


public class FlowGenMain {

	public static void main(String[] args) {

		// TODO refactor OpenFlow engine
		// TODO write test
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
//				OneIfaceFirewallVmRule rule = new OneIfaceFirewallVmRule(dpid, fwPort, vmPorts[i], vmMacs[i]);
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
