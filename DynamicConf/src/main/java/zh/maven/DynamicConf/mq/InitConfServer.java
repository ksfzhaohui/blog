package zh.maven.DynamicConf.mq;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitConfServer implements MessageListener {

	private static Logger LOGGER = LoggerFactory.getLogger(InitConfServer.class);
	private final String MQADDRESS = "tcp://127.0.0.1:61616";
	private final String QUEUE = "dynamicConfQueue";

	private QueueConnection connection = null;
	private QueueSession session = null;
	private Queue queue = null;

	public InitConfServer() {
		try {
			connection = createSharedConnection();
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			queue = session.createQueue(QUEUE);
			QueueReceiver receiver = session.createReceiver(queue);
			receiver.setMessageListener(this);
		} catch (Exception e) {
			LOGGER.error("init error", e);
		}
	}

	public void start() throws JMSException {
		connection.start();
		LOGGER.info("InitConfServer start");
	}

	/**
	 * 创建jms连接
	 * 
	 * @return
	 * @throws JMSException
	 */
	private QueueConnection createSharedConnection() throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQADDRESS);
		QueueConnection connection = null;
		try {
			connection = factory.createQueueConnection();
		} catch (JMSException e) {
			closeConnection(connection);
			throw e;
		}
		return connection;
	}

	/**
	 * 关闭连接
	 * 
	 * @param connection
	 */
	private void closeConnection(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			LOGGER.error("closeConnection error", e);
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			TextMessage receiveMessage = (TextMessage) message;
			String keys = receiveMessage.getText();
			LOGGER.info("keys = " + keys);
			MapMessage returnMess = session.createMapMessage();
			returnMess.setStringProperty("/a2/m1", "zhaohui");
			returnMess.setStringProperty("/a3/m1/v2", "nanjing");
			returnMess.setStringProperty("/a3/m1/v2/t2", "zhaohui");

			QueueSender sender = session.createSender((Queue) message.getJMSReplyTo());
			sender.send(returnMess);
		} catch (Exception e) {
			LOGGER.error("onMessage error", e);
		}
	}

	public static void main(String[] args) throws JMSException {
		InitConfServer server = new InitConfServer();
		server.start();
	}

}
