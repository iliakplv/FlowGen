package ru.spbstu.telematics.flowgen.cloud.rabbitmq;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.cloud.ICloud;
import ru.spbstu.telematics.flowgen.utils.StringUtils;

import java.io.IOException;

public class NovaNetworkQueueListener implements Runnable{

	private static final String INFO = "[INFO] ";
	private static final String ERROR = "[ERROR] ";

	private static final String EXCHANGE_NAME = "nova";
	private static final String EXCHANGE_TYPE = "topic";
	private static final boolean EXCHANGE_DURABLE = false;
	private static final boolean EXCHANGE_AUTO_DELETE = false;
	private static final boolean EXCHANGE_INTERNAL = false;
	private static final boolean QUEUE_EXCLUSIVE = false;

	private String host;

	private String queueName;
	private String queueRoutingKey;
	private boolean queueDurable;
	private boolean queueAutoDelete;

	private ICloud cloud;


	public NovaNetworkQueueListener(String host, String queueName, String queueRoutingKey,
									boolean queueDurable, boolean queueAutoDelete, ICloud cloud) {

		if (StringUtils.isNullOrEmpty(host)) {
			throw new IllegalArgumentException("Wrong host");
		}
		if (StringUtils.isNullOrEmpty(queueName)) {
			throw new IllegalArgumentException("Wrong queue name");
		}
		if (StringUtils.isNullOrEmpty(queueRoutingKey)) {
			throw new IllegalArgumentException("Wrong queue routing key");
		}
		if (cloud == null) {
			throw new NullPointerException("Cloud is null");
		}

		this.host = host;
		this.queueName = queueName;
		this.queueRoutingKey = queueRoutingKey;
		this.queueDurable = queueDurable;
		this.queueAutoDelete = queueAutoDelete;
		this.cloud = cloud;
	}

	@Override
	public void run() {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		// factory.setPort(5672);
		// factory.setVirtualHost("/");

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

			System.out.println("[AMQP] Host: " + host + ". Listening for queue \""
					+ queueName + "\" with routing key \"" + queueRoutingKey + "\"");

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();

				JSONObject message = null;
				try {
					message = new JSONObject(new String(delivery.getBody()));
				} catch (JSONException e) {
					System.out.println(ERROR + "Creating JSON from AMQP message failed:");
					e.printStackTrace();
					continue;
				}

				System.out.println("[AMQP] " + message.toString());

				NovaNetworkMessage networkMessage = NovaNetworkMessage.parse(message);

				if (networkMessage.getMessageType() == NovaNetworkMessage.MessageType.Launch) {
					cloud.findAndConnect(networkMessage.getIp());
				} else if (networkMessage.getMessageType() == NovaNetworkMessage.MessageType.Terminate) {
					cloud.findAndDisconnect(networkMessage.getIp());
				}
			}

		} catch (IOException e) {
			System.out.println(ERROR + "Connection exception caught:");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println(ERROR + "Interrupted exception caught:");
			e.printStackTrace();
		}
	}

}
