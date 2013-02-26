package ru.spbstu.telematics.flowgen;


import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.httpclient.HttpDeleteWithBody;
import ru.spbstu.telematics.flowgen.openflow.Datapath;
import ru.spbstu.telematics.flowgen.openflow.IDatapath;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class FlowGenMain {

	// TODO LOG

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

	}


	private static final String SFP_URL = "http://192.168.168.24:8080/wm/staticflowentrypusher/json";
	private static final String HEADER_CONTENT_TYPE_NAME = "content-type";
	private static final String HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

	public static void executeAdd(JSONObject command) {
		executeCommand(command, true);
	}

	public static void executeRemove(JSONObject command) {
		executeCommand(command, false);
	}

	private static void executeCommand(JSONObject command, boolean add) {
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpEntityEnclosingRequestBase request =  add ?
					new HttpPost(SFP_URL) :
					new HttpDeleteWithBody(SFP_URL);

			request.addHeader(HEADER_CONTENT_TYPE_NAME, HEADER_CONTENT_TYPE_VALUE);
			StringEntity params = new StringEntity(command.toString());
			request.setEntity(params);
			httpClient.execute(request);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

}
