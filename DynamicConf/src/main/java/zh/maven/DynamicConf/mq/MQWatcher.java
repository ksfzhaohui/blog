package zh.maven.DynamicConf.mq;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zh.maven.DynamicConf.AbstractWatcher;

/**
 * 基于MQ的参数通知
 * 
 * @author hui.zhao.cfs
 *
 */
public class MQWatcher extends AbstractWatcher implements ExceptionListener {
	private static Logger LOGGER = LoggerFactory.getLogger(MQWatcher.class);
	private final String MQADDRESS = "tcp://127.0.0.1:61616";
	private final String TOPIC = "dynamicConfTopic";
	private final String IDENTIFIER = "confKey";

	private Connection connection;
	private Session session;

	private final String QUEUE = "dynamicConfQueue";
	private QueueConnection queueConnection = null;
	private QueueSession queueSession = null;

	/** MQ的过滤器 **/
	private StringBuffer keyFilter = new StringBuffer();

	/**
	 * 监听所有keyPatterns
	 * 
	 * @throws Exception
	 */
	@Override
	public void watcherKeys() {
		try {
			connect();
			initKeyValues();
			watcherPaths();
		} catch (Exception e) {
			LOGGER.error("watcherKeys error", e);
		}
	}

	@Override
	public String getKeyValue(String key) {
		return keyValueMap.get(key);
	}

	@Override
	public void setKeyPatterns(String[] keyPatterns) {
		super.setKeyPatterns(keyPatterns);
		generateKeyFilter();
	}

	/**
	 * 将所有keyValue填充Properties
	 * 
	 * @param result
	 */
	@Override
	public void fillProperties(Properties result) {
		Iterator<String> keyItor = keyValueMap.keySet().iterator();
		while (keyItor.hasNext()) {
			String key = (String) keyItor.next();
			String v = (String) keyValueMap.get(key);
			result.put(key, v != null ? v : "");
		}
	}

	/**
	 * 初始化key-value值
	 * 
	 * @throws JMSException
	 */
	private void initKeyValues() throws JMSException {
		TemporaryQueue responseQueue = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;
		Queue queue = queueSession.createQueue(QUEUE);

		TextMessage requestMessage = queueSession.createTextMessage();
		requestMessage.setText(generateKeyString());
		responseQueue = queueSession.createTemporaryQueue();
		producer = queueSession.createProducer(queue);
		consumer = queueSession.createConsumer(responseQueue);
		requestMessage.setJMSReplyTo(responseQueue);
		producer.send(requestMessage);

		MapMessage receiveMap = (MapMessage) consumer.receive();
		@SuppressWarnings("unchecked")
		Enumeration<String> mapNames = receiveMap.getPropertyNames();
		while (mapNames.hasMoreElements()) {
			String key = mapNames.nextElement();
			String value = receiveMap.getStringProperty(key);
			keyValueMap.put(key, value);
			LOGGER.info("init key = " + key + ",value = " + value);
		}
	}

	/**
	 * 创建jms连接
	 * 
	 * @return
	 * @throws JMSException
	 */
	private Connection createSharedConnection() throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MQADDRESS);
		Connection connection = null;
		try {
			connection = factory.createConnection();
			connection.setExceptionListener(this);
		} catch (JMSException e) {
			closeConnection(connection);
			throw e;
		}
		return connection;
	}

	private QueueConnection createQueueConnection() throws JMSException {
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

	/**
	 * 连接start
	 * 
	 * @throws JMSException
	 */
	private void connect() throws JMSException {
		this.connection = createSharedConnection();
		this.connection.start();

		this.queueConnection = createQueueConnection();
		this.queueConnection.start();

		session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		queueSession = this.queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	/**
	 * 重新连接
	 * 
	 * @throws JMSException
	 */
	private void reconnect() throws JMSException {
		closeConnection(connection);
		connect();
	}

	private void watcherPaths() throws JMSException {
		Topic topic = session.createTopic(TOPIC);
		MessageConsumer consumer = session.createConsumer(topic, keyFilter.toString());
		consumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message message) {
				try {
					String key = message.getStringProperty(IDENTIFIER);
					TextMessage tm = (TextMessage) message;
					keyValueMap.put(key, tm.getText());
					LOGGER.info("key = " + key + ",value = " + tm.getText());
				} catch (JMSException e) {
					LOGGER.error("onMessage error", e);
				}
			}
		});
	}

	/**
	 * 生成接受过滤器
	 */
	private void generateKeyFilter() {
		for (int i = 0; i < keyPatterns.length; i++) {
			keyFilter.append(IDENTIFIER + " LIKE '" + keyPatterns[i] + "%'");
			if (i < keyPatterns.length - 1) {
				keyFilter.append(" OR ");
			}
		}
		LOGGER.info("keyFilter : " + keyFilter.toString());
	}

	private String generateKeyString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < keyPatterns.length; i++) {
			sb.append(keyPatterns[i]);
			if (i < keyPatterns.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	@Override
	public void onException(JMSException ex) {
		try {
			this.session = null;
			reconnect();
			watcherPaths();
			LOGGER.info("Successfully refreshed JMS Connection");
		} catch (JMSException recoverEx) {
			LOGGER.debug("Failed to recover JMS Connection", recoverEx);
			LOGGER.error("Encountered non-recoverable JMSException", ex);
		}
	}

}
