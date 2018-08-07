package zh.maven.jmsClient.queue.client;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class QReceiverListenerClient implements MessageListener {

	private QueueConnectionFactory factory;
	private QueueConnection qConnection;
	private QueueSession qSession;
	private Queue queue;
	private QueueReceiver qReceiver;

	public QReceiverListenerClient() {
		try {
			factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			qConnection = factory.createQueueConnection();
			qConnection.start();

			qSession = qConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
			queue = qSession.createQueue("test");
			qReceiver = qSession.createReceiver(queue);
			qReceiver.setMessageListener(this);
			System.out.println("等待接受消息......");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message message) {
		try {
			TextMessage textMessage = (TextMessage) message;
			System.out.println("消息内容：" + textMessage.getText() + ",是否重发：" + textMessage.getJMSRedelivered());
			textMessage.acknowledge();
			throw new RuntimeException("test");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new QReceiverListenerClient();
	}

}
