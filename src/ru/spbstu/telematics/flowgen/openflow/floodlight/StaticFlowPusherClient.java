package ru.spbstu.telematics.flowgen.openflow.floodlight;


import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.httpclient.HttpDeleteWithBody;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;
import ru.spbstu.telematics.flowgen.openflow.rules.Command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

public class StaticFlowPusherClient implements IStaticFlowPusherClient, IDatapathListener {

	private static final String URL_SCHEME = "http:";
	private static final String URL_STATIC_FLOW_PUSHER = "/wm/staticflowentrypusher/json";
	private static final String HTTP_HEADER_CONTENT_TYPE_NAME = "content-type";
	private static final String HTTP_HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

	private InetSocketAddress controllerAddress;


	/**
	 * Constructors
	 */

	public StaticFlowPusherClient(InetSocketAddress controllerAdress) {
		controllerAddress = controllerAdress;
	}

	public StaticFlowPusherClient(String controllerHostname, int controllerPort) {
		this(new InetSocketAddress(controllerHostname, controllerPort));
	}


	/**
	 * Controller address
	 */

	public InetSocketAddress getControllerAddress() {
		return controllerAddress;
	}

	public void setControllerAddress(InetSocketAddress controllerAddress) {
		this.controllerAddress = controllerAddress;
	}

	public void setControllerHostname(String hostname) {
		controllerAddress = new InetSocketAddress(hostname, controllerAddress.getPort());
	}

	public void setControllerPort(int port) {
		controllerAddress = new InetSocketAddress(controllerAddress.getAddress(), port);
	}


	/**
	 * Static Flow Pusher address
	 */

	public String getStaticFlowPusherUrl() {
		String controllerAddress = this.controllerAddress.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(URL_SCHEME);
		sb.append(controllerAddress.startsWith("/") ? "/" : "//");
		sb.append(controllerAddress);
		sb.append(URL_STATIC_FLOW_PUSHER);
		return sb.toString();
	}


	private synchronized void executeCommand(JSONObject command, CommandType commandType) {
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpEntityEnclosingRequestBase request;
			String url = getStaticFlowPusherUrl();
			if (commandType == CommandType.FLOW_ADD) {
				request = new HttpPost(url);
			} else if (commandType == CommandType.FLOW_REMOVE) {
				request = new HttpDeleteWithBody(url);
			} else {
				throw new IllegalArgumentException("Unknown command type " + commandType);
			}

			request.addHeader(HTTP_HEADER_CONTENT_TYPE_NAME, HTTP_HEADER_CONTENT_TYPE_VALUE);
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


	/**
	 * IStaticFlowPusherClient implementation
	 */

	// TODO [second] fix implementation to use Command.java

	@Override
	public void addFlow(JSONObject command) {
		executeCommand(command, CommandType.FLOW_ADD);
	}

	@Override
	public void addFlows(JSONObject[] commands) {
		for (JSONObject command : commands) {
			executeCommand(command, CommandType.FLOW_ADD);
		}
	}

	@Override
	public void removeFlow(JSONObject command) {
		executeCommand(command, CommandType.FLOW_REMOVE);
	}

	@Override
	public void removeFlows(JSONObject[] commands) {
		for (JSONObject command : commands) {
			executeCommand(command, CommandType.FLOW_REMOVE);
		}
	}


	/**
	 * IDatapathListener implementation
	 */

	@Override
	public void onConnection(JSONObject[] commands) {
		addFlows(commands);
	}

	@Override
	public void onDisconnection(JSONObject[] commands) {
		removeFlows(commands);
	}
}
