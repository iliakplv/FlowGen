package ru.spbstu.telematics.flowgen.application;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.application.configuration.CloudConfig;
import ru.spbstu.telematics.flowgen.application.configuration.DatapathConfig;
import ru.spbstu.telematics.flowgen.application.configuration.DeviceConfig;
import ru.spbstu.telematics.flowgen.application.configuration.FloodlightConfig;
import ru.spbstu.telematics.flowgen.application.configuration.ServerConfig;
import ru.spbstu.telematics.flowgen.cloud.Cloud;
import ru.spbstu.telematics.flowgen.cloud.ICloud;
import ru.spbstu.telematics.flowgen.cloud.rabbitmq.NovaNetworkQueueListener;
import ru.spbstu.telematics.flowgen.openflow.datapath.Datapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
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
import java.util.HashMap;
import java.util.List;
import java.util.Set;


// TODO IDatapath -> [sub]
// TODO IDatapath concurrency research
// TODO [low] IDatapath safe host migration

// TODO ICloud concurrency research
// TODO [low] ICloud support implemented IDatapath migration

// TODO ALL log and exception messages


public class FlowGenMain {

	private static final String DP_LOGGER_TAG = "*";

	private static CloudConfig cloudConfig;
	private static Cloud cloud;

	private static boolean FLOW_PUSHING = false;
	private static boolean LISTEN_NOVA = false;


	public static void main(String[] args) {

//		String fileName = args[0];
//		String fileName = "/home/ilya/workspace/FlowGen/cloud.json";
		String fileName = "C:\\Users\\Kopylov\\workspace\\Projects\\FlowGen\\cloud.json";

		if (StringUtils.isNullOrEmpty(fileName)) {
			System.out.println("Bad config file name.");
		} else {

			System.out.println("Cloud config file: " + fileName + "\n");
			parseParams(fileName);
			startApplication();

		}
	}

	private static void parseParams(String fileName) {

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
		if (FLOW_PUSHING) {
			cloud.addDatapathListener(floodlightClient);
			System.out.println("Datapath listener (" + floodlightClient.getControllerAddress().toString() + ") added to cloud " + cloud.toString());
		} else {
			System.out.println("No datapath listener added to cloud " + cloud.toString() + ". Flows pushing disabled");
		}
		cloud.addDatapathListener(new DatapathLogger(DP_LOGGER_TAG));
		System.out.println("Datapath logger (TAG=\"" + DP_LOGGER_TAG + "\") added to cloud " + cloud.toString());

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
			datapath.connectToNetwork();
			System.out.println("Datapath (" + datapath.toString() + ") connected to network");

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
		List<ServerConfig> servers = cloudConfig.getServers();
		ServerConfig serverConfig = servers.isEmpty() ? null : servers.get(0);
		boolean attachNovaListener = LISTEN_NOVA && serverConfig != null;
		if (attachNovaListener) {
			NovaNetworkQueueListener novaListener = new NovaNetworkQueueListener(serverConfig.getHost(),
					serverConfig.getQueueName(),
					serverConfig.getRoutingKey(),
					false,
					false);
			cloud.setNovaListener(novaListener);
			cloud.startListeningNova();
			System.out.println("Nova network queue listener attached to cloud " + cloud.toString() +
					" and listening for messages");

		} else if (!LISTEN_NOVA) {
			System.out.println("No Nova network queue listener attached to cloud " + cloud.toString() +
					". Listening disabled.");
		} else {
			System.out.println("No Nova network queue listener attached to cloud " + cloud.toString() +
					". Listener not specified in config file.");
		}

		if (attachNovaListener) {
			System.out.println("Initialization is done. Application is running...\n");
		} else {
			System.out.println("Initialization is done. Nothing to do. Shutting down...\n");
		}
	}


	/**************************************************
	 *
	 *					TESTS
	 *
	 **************************************************/

	private static void testVn0() {

		// Datapath

		String dpid = "00:00:00:15:17:f9:4c:7f";
		String name = "br-int";
		int trunkPort = 5;
		int firewallPort = 48;
		String gwMac = "fa:16:3e:15:2d:df";
		IDatapath datapath = new Datapath(dpid, name, trunkPort, firewallPort, gwMac);

		// Cloud

		String cloudName = "vn0";
		ICloud cloud = new Cloud(cloudName);
		cloud.addDatapath(datapath);
		cloud.addDatapathListener(new DatapathLogger(datapath.toString()));

		// SFP client

		FloodlightClient flClient = new FloodlightClient("127.0.0.1", 8080);
		cloud.setFloodlightClient(flClient);

		// Hosts (not VMs)

		HashMap<String, Integer> portMacMap = new HashMap<String, Integer>();
		portMacMap.put("5c:d9:98:37:16:02", 1); // eth0
		portMacMap.put("fa:16:3e:77:56:6e", 2); // gw-fb259ed4-dd
		Set<String> macs = portMacMap.keySet();


//		Adding flows

//		REGISTER TO ADD
//		cloud.addDatapathListener(flClient);

		cloud.getDatapath(dpid).connectToNetwork();
		for (String mac : macs) {
			cloud.launchGateway(mac, datapath.getDpid(), portMacMap.get(mac));
		}


//		FLOWGEN !!!

		NovaNetworkQueueListener novaListener = new NovaNetworkQueueListener("vn0",
						"ovs.network.vn0",
						"network.vn0",
						false,
						false);
		cloud.setNovaListener(novaListener);
		cloud.startListeningNova();


//		CONFIG !!!

		if (true) {
			CloudConfig cc = cloud.getConfig();
			System.out.println("[CONFIG]\n" + cc.export().toString());
			return;
		}


//		UNREGISTER TO KEEP
//		cloud.deleteDatapathListener(flClient);


//		Removing flows

//		REGISTER TO REMOVE
//		cloud.addDatapathListener(flClient);

		for (String mac : macs) {
			cloud.stopDevice(mac);
		}
		cloud.getDatapath(dpid).disconnectFromNetwork();
	}

	private static void testRabbitMq() {

		final String host = "vn0";
		final String exchange = "nova";
		final String queueNamePrefix = "ovs.";

		String[] routingKeys = new String[]{
//				"network",
				"network.vn0",
				"compute",
				"compute.vn0",
				"scheduler",
				"scheduler.vn0"};

		for (String routingKey : routingKeys) {
			NovaRabbitMqListener listener = new NovaRabbitMqListener(host,
					exchange,
					queueNamePrefix + routingKey,
					routingKey,
					false,
					false);
			listener.start();
		}

	}

	private static class NovaRabbitMqListener extends Thread {

		private static final String EXCHANGE_TYPE = "topic";
		private static final boolean EXCHANGE_DURABLE = false;
		private static final boolean EXCHANGE_AUTO_DELETE = false;
		private static final boolean EXCHANGE_INTERNAL = false;

		private String host;
		private String exchangeName;
		private String queueName;
		private String queueRoutingKey;
		private boolean queueDurable;
		private boolean queueAutoDelete;
		private static final boolean QUEUE_EXCLUSIVE = false;

		public NovaRabbitMqListener(String host, String exchangeName, String queueName, String queueRoutingKey,
									boolean queueDurable, boolean queueAutoDelete) {
			this.host = host;
			this.exchangeName = exchangeName;
			this.queueName = queueName;
			this.queueRoutingKey = queueRoutingKey;
			this.queueDurable = queueDurable;
			this.queueAutoDelete = queueAutoDelete;
		}

		@Override
		public void run() {

			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(host);
//			factory.setPort(5672);
//			factory.setVirtualHost("/");

			Connection connection;
			Channel channel;

			try {
				connection = factory.newConnection();
				channel = connection.createChannel();

				channel.exchangeDeclare(exchangeName,
						EXCHANGE_TYPE,
						EXCHANGE_DURABLE,
						EXCHANGE_AUTO_DELETE,
						EXCHANGE_INTERNAL,
						null);

//				channel.queueDelete(QUEUE_NAME);

				channel.queueDeclare(queueName,
						queueDurable,
						QUEUE_EXCLUSIVE,
						queueAutoDelete,
						null);

				channel.queueBind(queueName,
						exchangeName,
						queueRoutingKey,
						null);

				QueueingConsumer consumer = new QueueingConsumer(channel);
				channel.basicConsume(queueName, true, consumer);

				System.out.println("Listening for queue \"" + queueName +
						"\" with routing key \"" + queueRoutingKey + "\"");

				while (true) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					String message = new String(delivery.getBody());
					System.out.println("\t[ " + queueRoutingKey + " ]\n" + message);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
