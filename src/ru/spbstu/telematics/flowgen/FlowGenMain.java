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

		try {
			testRabbitMQ();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

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

	public static void testRabbitMQ() throws IOException, InterruptedException {

		final String HOST = "vn0";

		// Must be exactly the same as params of target exchange
		final String EXCHANGE_NAME = "nova";
		final String EXCHANGE_TYPE = "topic";
		final boolean EXCHANGE_DURABLE = false;
		final boolean EXCHANGE_AUTO_DELETE = false;
		final boolean EXCHANGE_INTERNAL = false;

		final String QUEUE_NAME = "ovs.network";
		// Must be exactly the same as params of target queue
		final String QUEUE_ROUTING_KEY = "network";	// target queue name
		final boolean QUEUE_DURABLE = false;
		final boolean QUEUE_AUTO_DELETE = false;
		final boolean QUEUE_EXCLUSIVE = false;


		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(HOST);
//		factory.setPort(5672);
//		factory.setVirtualHost("/");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(EXCHANGE_NAME,
				EXCHANGE_TYPE,
				EXCHANGE_DURABLE,
				EXCHANGE_AUTO_DELETE,
				EXCHANGE_INTERNAL,
				null);

//		channel.queueDelete(QUEUE_NAME);

		channel.queueDeclare(QUEUE_NAME,
				QUEUE_DURABLE,
				QUEUE_EXCLUSIVE,
				QUEUE_AUTO_DELETE,
				null);

		channel.queueBind(QUEUE_NAME,
				EXCHANGE_NAME,
				QUEUE_ROUTING_KEY,
				null);

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(QUEUE_NAME, true, consumer);

		System.out.println("Waiting for messages...");
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			System.out.println(message);
		}

	}

}
