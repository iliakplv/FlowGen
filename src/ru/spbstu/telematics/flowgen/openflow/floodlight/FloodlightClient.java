package ru.spbstu.telematics.flowgen.openflow.floodlight;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import ru.spbstu.telematics.flowgen.application.configuration.FloodlightConfig;
import ru.spbstu.telematics.flowgen.httpclient.HttpDeleteWithBody;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapathListener;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.ControllerData;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.Hosts;
import ru.spbstu.telematics.flowgen.openflow.rules.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

public class FloodlightClient implements IFloodlightClient, IDatapathListener {

	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String URL_SCHEME = "http:";
	private static final String URL_STATIC_FLOW_PUSHER =	"/wm/staticflowentrypusher/json";
	private static final String URL_GET_ALL_DATAPATHS =		"/wm/core/controller/switches/json";
	private static final String URL_GET_ALL_DEVICES =		"/wm/device/";
	private static final String HTTP_HEADER_CONTENT_TYPE_NAME = "content-type";
	private static final String HTTP_HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";

	private static final String REQUEST_FAILED = "[CRITICAL] Request to controller failed!";
	private static final String EXECUTION_FAILED = "[CRITICAL] Command execution failed!";

	private InetSocketAddress controllerAddress;


	/**
	 * Constructors
	 */

	public FloodlightClient(InetSocketAddress controllerAdress) {
		controllerAddress = controllerAdress;
	}

	public FloodlightClient(String controllerHostname, int controllerPort) {
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
	 * URLs
	 */

	public String getControllerUrl() {
		String controllerAddress = this.controllerAddress.toString();
		StringBuilder sb = new StringBuilder();
		sb.append(URL_SCHEME);
		sb.append(controllerAddress.startsWith("/") ? "/" : "//");
		sb.append(controllerAddress);
		return sb.toString();
	}

	public String getStaticFlowPusherUrl() {
		return getControllerUrl() + URL_STATIC_FLOW_PUSHER;
	}

	public String getAllDatapathsUrl() {
		return getControllerUrl() + URL_GET_ALL_DATAPATHS;
	}

	public String getAllDevicesUrl() {
		return getControllerUrl() + URL_GET_ALL_DEVICES;
	}


	/**
	 * Command executor
	 */

	private synchronized void executeCommand(JSONObject command, Command.Action action) {
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpEntityEnclosingRequestBase request;
			String url = getStaticFlowPusherUrl();
			if (action == Command.Action.FlowAdd) {
				request = new HttpPost(url);
			} else if (action == Command.Action.FlowRemove) {
				request = new HttpDeleteWithBody(url);
			} else {
				throw new IllegalArgumentException("Unknown command action " + action);
			}

			request.addHeader(HTTP_HEADER_CONTENT_TYPE_NAME, HTTP_HEADER_CONTENT_TYPE_VALUE);
			StringEntity params = new StringEntity(command.toString());
			request.setEntity(params);
			httpClient.execute(request);

		} catch (UnsupportedEncodingException e) {
			System.out.println(EXECUTION_FAILED);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(EXECUTION_FAILED);
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}


	/**
	 * IFloodlightClient implementation
	 */

	@Override
	public void addFlow(JSONObject command) {
		executeCommand(command, Command.Action.FlowAdd);
	}

	@Override
	public void addFlows(JSONObject[] commands) {
		for (JSONObject command : commands) {
			executeCommand(command, Command.Action.FlowAdd);
		}
	}

	@Override
	public void removeFlow(JSONObject command) {
		executeCommand(command, Command.Action.FlowRemove);
	}

	@Override
	public void removeFlows(JSONObject[] commands) {
		for (JSONObject command : commands) {
			executeCommand(command, Command.Action.FlowRemove);
		}
	}

	private synchronized JSONArray requestToController(String url) {

		HttpClient httpClient = new DefaultHttpClient();

		JSONArray result = null;
		try {

			HttpGet request = new HttpGet(url);
			request.addHeader(HTTP_HEADER_CONTENT_TYPE_NAME, HTTP_HEADER_CONTENT_TYPE_VALUE);

			HttpResponse response = httpClient.execute(request);

			InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), DEFAULT_ENCODING);
			BufferedReader br = new BufferedReader(isr);
			String jsonString = br.readLine();
			JSONTokener tokener = new JSONTokener(jsonString);
			result = new JSONArray(tokener);

		} catch (UnsupportedEncodingException e) {
			System.out.println(REQUEST_FAILED);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(REQUEST_FAILED);
			e.printStackTrace();
		} catch (JSONException e ) {
			System.out.println(REQUEST_FAILED);
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return result;
	}

	@Override
	public ControllerData getControllerData() {
		return ControllerData.parse(requestToController(getAllDatapathsUrl()));
	}

	@Override
	public Hosts getKnownHosts() {
		return Hosts.parse(requestToController(getAllDevicesUrl()));
	}

	@Override
	public FloodlightConfig getConfig() {
		return new FloodlightConfig(controllerAddress.getHostName(), controllerAddress.getPort(), true);
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
