package ru.spbstu.telematics.flowgen;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import ru.spbstu.telematics.flowgen.cloud.Cloud;
import ru.spbstu.telematics.flowgen.cloud.ICloud;
import ru.spbstu.telematics.flowgen.openflow.datapath.Datapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.floodlight.FloodlightClient;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.ControllerData;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.DatapathData;
import ru.spbstu.telematics.flowgen.openflow.floodlight.topology.PortData;
import ru.spbstu.telematics.flowgen.utils.DatapathLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;


public class FlowGenMain {

	public static void main(String[] args) {

		testVn0();

//		testRabbitMq();

	}

	public static void testVn0() {

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

		// VMs

		HashMap<String, Integer> portMacMap = new HashMap<String, Integer>();
		portMacMap.put("5c:d9:98:37:16:02", 1); // eth0
		portMacMap.put("fa:16:3e:77:56:6e", 2); // gw-fb259ed4-dd
		portMacMap.put("fa:16:3e:69:ab:bf", 49);
		portMacMap.put("fa:16:3e:38:0f:e9", 50);
		Set<String> macs = portMacMap.keySet();


//		Adding flows

//		REGISTER TO ADD
//		cloud.addDatapathListener(flClient);

		cloud.getDatapath(dpid).connectToNetwork();
		for (String mac : macs) {
			cloud.launchVm(mac, datapath.getDpid(), portMacMap.get(mac));
//			cloud.launchVmByMac(mac);
		}

//		PARSING TEST
		if (true) {
			try {
				parsingTest(flClient.getAllConnectedHosts());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}


//		UNREGISTER TO KEEP
//		cloud.deleteDatapathListener(flClient);


//		Removing flows

//		REGISTER TO REMOVE
//		cloud.addDatapathListener(flClient);

		for (String mac : macs) {
			cloud.stopVm(mac);
		}
		cloud.getDatapath(dpid).disconnectFromNetwork();
	}

	public static void testRabbitMq() {

		final String host = "vn0";
//		final String host = "host_ip";
		final String exchange = "nova";
//		final String exchange = "quantum";
		final String queueNamePrefix = "ovs.";

		String[] routingKeys = new String[]{
				//"network",
				"network.vn0",
				"compute",
				"compute.vn0",
				"scheduler",
				"scheduler.vn0"};
//		String[] routingKeys = new String[] {"q-plugin"};

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

	public static void parsingTest(JSONArray data) throws JSONException {

		ControllerData controllerData = ControllerData.parse(data);

		for (DatapathData datapathData : controllerData.getDatapaths()) {

			System.out.println("\n[DPID] " + datapathData.getDpid());

			for (PortData portData : datapathData.getPorts()) {
				System.out.println(portData.getNumber() + " " +
						portData.getName() + " " +
						portData.getMac() + " " +
						(portData.isDatapathReservedPort() ? "(datapath)" : ""));
			}
		}

		System.out.println();
	}


	/***** Inner classes *****/

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
