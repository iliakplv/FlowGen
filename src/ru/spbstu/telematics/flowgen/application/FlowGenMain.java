package ru.spbstu.telematics.flowgen.application;


import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.application.configuration.CloudConfig;
import ru.spbstu.telematics.flowgen.application.configuration.DatapathConfig;
import ru.spbstu.telematics.flowgen.application.configuration.DeviceConfig;
import ru.spbstu.telematics.flowgen.application.configuration.FloodlightConfig;
import ru.spbstu.telematics.flowgen.application.configuration.ServerConfig;
import ru.spbstu.telematics.flowgen.cloud.Cloud;
import ru.spbstu.telematics.flowgen.cloud.rabbitmq.NovaNetworkQueueListener;
import ru.spbstu.telematics.flowgen.openflow.datapath.Datapath;
import ru.spbstu.telematics.flowgen.openflow.floodlight.FloodlightClient;
import ru.spbstu.telematics.flowgen.utils.DatapathLogger;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;


public class FlowGenMain {

	private static CloudConfig cloudConfig;
	private static Cloud cloud;


	public static void main(String[] args) {

		String fileName = args[0];

		if (StringUtils.isNullOrEmpty(fileName)) {
			System.out.println("\nBad config file name.\n");
		} else {

			// RUN!

			System.out.println("\nCloud config file: " + fileName + "\n");
			parseConfig(fileName);
			startApplication();

		}
	}

	private static void parseConfig(String fileName) {

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(new File(fileName));
			FileChannel channel = stream.getChannel();
			MappedByteBuffer bb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			String paramsFile = Charset.defaultCharset().decode(bb).toString();

			JSONObject paramsJson = new JSONObject(paramsFile);

			cloudConfig = CloudConfig.parse(paramsJson);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void startApplication() {
		if (cloudConfig != null) {
			System.out.println("Starting application...");
		} else {
			System.out.println("No cloud config. Shutting down...");
			return;
		}

		// Cloud
		cloud = new Cloud(cloudConfig.getName());
		System.out.println("Cloud (" + cloud.toString() + ") created");
		FloodlightConfig floodlightConfig = cloudConfig.getFloodlight();
		FloodlightClient floodlightClient = new FloodlightClient(floodlightConfig.getHost(), floodlightConfig.getPort());
		cloud.setFloodlightClient(floodlightClient);
		System.out.println("Floodlight client (" + floodlightClient.getControllerAddress().toString() + ") added to cloud " + cloud.toString());
		if (floodlightConfig.isPushingFlows()) {
			cloud.addDatapathListener(floodlightClient);
			System.out.println("Datapath listener (" + floodlightClient.getControllerAddress().toString() + ") added to cloud " + cloud.toString());
		} else {
			System.out.println("No datapath listener added to cloud " + cloud.toString() + ". Flows pushing disabled");
		}
		String datapathLoggerTag = cloud.getName();
		cloud.addDatapathListener(new DatapathLogger(datapathLoggerTag));
		System.out.println("Datapath logger (TAG=\"" + datapathLoggerTag + "\") added to cloud " + cloud.toString());

		// Datapaths
		List<DatapathConfig> datapaths = cloudConfig.getDatapaths();
		for (DatapathConfig datapathConfig : datapaths) {

			// Creating Datapath
			Datapath datapath = new Datapath(datapathConfig.getDpid(),
					datapathConfig.getName(),
					datapathConfig.getTrunkPort(),
					datapathConfig.getFirewallPort(),
					datapathConfig.getGatewayMac());
			// Adding Datapath to Cloud
			cloud.addDatapath(datapath);
			System.out.println("Datapath (" + datapath.toString() + ") added to cloud " + cloud.toString());
			datapath.connectToNetwork(datapathConfig.isConnectedToSubnet());
			System.out.println("Datapath (" + datapath.toString() + ") connected to network (" +
					(datapathConfig.isConnectedToSubnet() ? "and" : "not") + " connected to subnet)");

			// Adding Gateways
			List<DeviceConfig> devices = datapathConfig.getGateways();
			for (DeviceConfig deviceConfig : devices) {
				cloud.launchGateway(deviceConfig.getMac(),
						datapathConfig.getDpid(),
						deviceConfig.getPort());
				System.out.println("Gateway with MAC (" + deviceConfig.getMac() +
						") attached to port (" + deviceConfig.getPort() +
						") of datapath " + datapath.toString());
				if (!deviceConfig.isActive()) {
					cloud.pauseDevice(deviceConfig.getMac());
					System.out.println("Gateway with MAC (" + deviceConfig.getMac() +
							") paused on port (" + deviceConfig.getPort() +
							") of datapath " + datapath.toString());
				}
			}
			// Adding Hosts
			devices = datapathConfig.getHosts();
			for (DeviceConfig deviceConfig : devices) {
				cloud.launchHost(deviceConfig.getMac(),
						datapathConfig.getDpid(),
						deviceConfig.getPort());
				System.out.println("Host with MAC (" + deviceConfig.getMac() +
						") attached to port (" + deviceConfig.getPort() +
						") of datapath " + datapath.toString());
				if (!deviceConfig.isActive()) {
					cloud.pauseDevice(deviceConfig.getMac());
					System.out.println("Host with MAC (" + deviceConfig.getMac() +
							") paused port (" + deviceConfig.getPort() +
							") of datapath " + datapath.toString());
				}
			}

		}

		// Cloud Server
		boolean listenerAttached = false;
		List<ServerConfig> servers = cloudConfig.getServers();
		for (ServerConfig serverConfig : servers) {

			if (serverConfig.isActive()) {
				NovaNetworkQueueListener novaListener = new NovaNetworkQueueListener(serverConfig.getHost(),
						serverConfig.getPort(),
						serverConfig.getQueueName(),
						serverConfig.getRoutingKey(),
						false,
						false);

				cloud.addNovaListener(novaListener);

				System.out.println("Nova network queue listener attached to cloud " + cloud.toString() +
						" and listening for messages");

				listenerAttached = true;
			}
		}

		if (listenerAttached) {
			System.out.println("Initialization is done. Application is running...\n");
		} else {
			System.out.println("Initialization is done. No active Nova listeners. Shutting down...\n");
		}
	}

}
