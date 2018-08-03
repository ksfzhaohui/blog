package zh.maven.jmsClient.queue;

import java.io.IOException;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class QReceiver {

	private QueueConnectionFactory factory;
	private QueueConnection qConnection;
	private QueueSession qSession;
	private Queue queue;
	private QueueReceiver qReceiver;

	public QReceiver() {
		try {
			factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			qConnection = factory.createQueueConnection();
			qConnection.start();

			qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			queue = qSession.createQueue("test");
			qReceiver = qSession.createReceiver(queue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void receiver() {
		try {
			while (true) {
				TextMessage message = (TextMessage) qReceiver.receive(10000);
				if (null != message) {
					System.out.println("收到消息:" + message.getText());
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new QReceiver().receiver();
	}
}
