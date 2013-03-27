package ru.spbstu.telematics.flowgen;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import ru.spbstu.telematics.flowgen.openflow.datapath.Datapath;
import ru.spbstu.telematics.flowgen.openflow.datapath.IDatapath;
import ru.spbstu.telematics.flowgen.openflow.floodlight.StaticFlowPusherClient;
import ru.spbstu.telematics.flowgen.utils.DatapathLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;


public class FlowGenMain {

	public static void main(String[] args) {

//		testVn0();

		testRabbitMq();

	}

	public static void testVn0() {

		// Datapath, Gateway

		String dpid = "00:00:a6:49:24:26:a5:40";
		String name = "qbr1dee26dc-b0";
		int trunkPort = 1;
		int firewallPort = 3;
		String gwMac = "fa:16:3e:15:2d:df";
		IDatapath datapath = new Datapath(dpid, name, trunkPort, firewallPort, gwMac);
		datapath.registerListener(new DatapathLogger(dpid, name));

		// SFP client

		StaticFlowPusherClient sfpClient = new StaticFlowPusherClient("127.0.0.1", 8080);

		// VMs

		HashMap<Integer, String> vmPortMacMap = new HashMap<Integer, String>();
		vmPortMacMap.put(4, "fa:16:3e:69:ab:bf");
		vmPortMacMap.put(5, "fa:16:3e:38:0f:e9");
		Set<Integer> ports = vmPortMacMap.keySet();



		// Adding flows

//		REGISTER TO ADD
//		datapath.registerListener(sfpClient);

		datapath.connectToNetwork();
		for (int port : ports) {
			datapath.connectVm(vmPortMacMap.get(port), port);
		}

//		UNREGISTER TO KEEP
//		datapath.unregisterListener(sfpClient);



		// Removing flows

//		REGISTER TO REMOVE
//		datapath.registerListener(sfpClient);

		for (int port : ports) {
			datapath.disconnectVm(port);
		}
		datapath.disconnectFromNetwork();
	}

	public static void testRabbitMq() {

		final String host = "vn0";
		final String queueNamePrefix = "ovs.";

		String[] routingKeys = new String[]{
				//"network",
				"network.vn0",
				"compute",
				"compute.vn0",
				"scheduler",
				"scheduler.vn0"};

		for (String routingKey : routingKeys) {
			NovaRabbitMqListener listener = new NovaRabbitMqListener(host,
					queueNamePrefix + routingKey,
					routingKey,
					false,
					false);
			listener.start();
		}

	}

	/***** Inner classes *****/

	private static class NovaRabbitMqListener extends Thread {

		private static final String EXCHANGE_NAME = "nova";
		private static final String EXCHANGE_TYPE = "topic";
		private static final boolean EXCHANGE_DURABLE = false;
		private static final boolean EXCHANGE_AUTO_DELETE = false;
		private static final boolean EXCHANGE_INTERNAL = false;

		private String host;

		private String queueName;
		private String queueRoutingKey;
		private boolean queueDurable;
		private boolean queueAutoDelete;
		private static final boolean QUEUE_EXCLUSIVE = false;

		public NovaRabbitMqListener(String host, String queueName, String queueRoutingKey,
									boolean queueDurable, boolean queueAutoDelete) {
			this.host = host;
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

				channel.exchangeDeclare(EXCHANGE_NAME,
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
						EXCHANGE_NAME,
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
