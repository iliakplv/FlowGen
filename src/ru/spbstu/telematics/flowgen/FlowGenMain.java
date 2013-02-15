package ru.spbstu.telematics.flowgen;


import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.spbstu.telematics.flowgen.openflow.FirewallRule;
import ru.spbstu.telematics.flowgen.openflow.OneIfaceFirewallRule;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public class FlowGenMain {

	public static void main(String[] args) {

		String sfpUrl =	"http://192.168.168.24:8080/wm/staticflowentrypusher/json";
		String dpid = "00:00:b6:60:ff:e5:93:4f";
		int fwPort = 1;

		int[] vmPorts = new int[]	{2};
		String[] vmMacs = 			{"12:34:65:78:9A:B0"};

		HttpClient httpClient = new DefaultHttpClient();
		try {
			for (int i = 0; i < vmPorts.length; i++) {
				OneIfaceFirewallRule rule = new OneIfaceFirewallRule(dpid, fwPort, vmPorts[i], vmMacs[i]);

				HttpPost inRequest = new HttpPost(sfpUrl);
				inRequest.addHeader("content-type", "application/x-www-form-urlencoded");
				StringEntity inParams = new StringEntity(rule.ovsInFlowAddCommand().toString());
				inRequest.setEntity(inParams);
				httpClient.execute(inRequest);

				HttpPost outRequest = new HttpPost(sfpUrl);
				inRequest.addHeader("content-type", "application/x-www-form-urlencoded");
				StringEntity outParams = new StringEntity(rule.ovsOutFlowAddCommand().toString());
				outRequest.setEntity(outParams);
				httpClient.execute(outRequest);
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

	}

}
