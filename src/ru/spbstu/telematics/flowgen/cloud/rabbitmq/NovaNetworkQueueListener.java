package ru.spbstu.telematics.flowgen.cloud.rabbitmq;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.JSONException;
import org.json.JSONObject;
import ru.spbstu.telematics.flowgen.application.configuration.ServerConfig;
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
	private int port;

	private String queueName;
	private String queueRoutingKey;
	private boolean queueDurable;
	private boolean queueAutoDelete;

	private ICloud cloud;


	public NovaNetworkQueueListener(String host, int port, String queueName, String queueRoutingKey,
									boolean queueDurable, boolean queueAutoDelete) {

		if (StringUtils.isNullOrEmpty(host)) {
			throw new IllegalArgumentException("Wrong host");
		}
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Wrong port number: " + port);
		}
		if (StringUtils.isNullOrEmpty(queueName)) {
			throw new IllegalArgumentException("Wrong queue name");
		}
		if (StringUtils.isNullOrEmpty(queueRoutingKey)) {
			throw new IllegalArgumentException("Wrong queue routing key");
		}

		this.host = host;
		this.port = port;
		this.queueName = queueName;
		this.queueRoutingKey = queueRoutingKey;
		this.queueDurable = queueDurable;
		this.queueAutoDelete = queueAutoDelete;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getQueueRoutingKey() {
		return queueRoutingKey;
	}

	public void setQueueRoutingKey(String queueRoutingKey) {
		this.queueRoutingKey = queueRoutingKey;
	}

	public boolean isQueueDurable() {
		return queueDurable;
	}

	public void setQueueDurable(boolean queueDurable) {
		this.queueDurable = queueDurable;
	}

	public boolean isQueueAutoDelete() {
		return queueAutoDelete;
	}

	public void setQueueAutoDelete(boolean queueAutoDelete) {
		this.queueAutoDelete = queueAutoDelete;
	}

	public ICloud getCloud() {
		return cloud;
	}

	public void setCloud(ICloud cloud) {
		this.cloud = cloud;
	}

	@Override
	public void run() {

		if(cloud == null) {
			System.out.println(INFO + "Can't start listening for Nova network (no Cloud set). Listener: " + toString());
			return;
		}

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(port);
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

				if (networkMessage != null) {
					NovaNetworkMessage.MessageType messageType = networkMessage.getMessageType();
					if (messageType == NovaNetworkMessage.MessageType.Launch) {
						cloud.findAndConnect(networkMessage.getIp());
					} else if (messageType == NovaNetworkMessage.MessageType.Terminate) {
						cloud.findAndDisconnect(networkMessage.getIp());
					}
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

	public ServerConfig getConfig() {
		return new ServerConfig(host, port, queueName, queueRoutingKey, true);
	}


	/***** Other *****/

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		NovaNetworkQueueListener listener = (NovaNetworkQueueListener) o;

		if (port != listener.port) return false;
		if (!host.equals(listener.host)) return false;
		if (!queueName.equals(listener.queueName)) return false;
		if (!queueRoutingKey.equals(listener.queueRoutingKey)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = host.hashCode();
		result = 31 * result + port;
		result = 31 * result + queueName.hashCode();
		result = 31 * result + queueRoutingKey.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "NovaNetworkQueueListener{" +
				"port=" + port +
				", host='" + host + '\'' +
				", queueName='" + queueName + '\'' +
				", queueRoutingKey='" + queueRoutingKey + '\'' +
				'}';
	}
}
