package ru.spbstu.telematics.flowgen;


import org.apache.http.HttpResponse;
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

		FirewallRule rule = new OneIfaceFirewallRule("00:00:00:00:00:00:00:01", 1, 2, "12:34:65:78:9A:B0");

		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpPost request = new HttpPost("http://localhost:8080/wm/staticflowentrypusher/json");
			StringEntity params = new StringEntity(rule.ovsInFlowAddCommand().toString());
			request.addHeader("content-type", "application/x-www-form-urlencoded");
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);

			// ...
		} catch (UnsupportedEncodingException e) {
			// ...
		} catch (IOException e) {
			// ...
		} finally {
			httpClient.getConnectionManager().shutdown();
		}



	}
}
