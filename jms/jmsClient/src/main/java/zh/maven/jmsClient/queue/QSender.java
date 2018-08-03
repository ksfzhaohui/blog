package zh.maven.jmsClient.queue;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class QSender {

	private QueueConnectionFactory factory;
	private QueueConnection qConnection;
	private QueueSession qSession;
	private Queue queue;
	private QueueSender qSender;

	public QSender() {
		try {
			factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			qConnection = factory.createQueueConnection();
			qConnection.start();

			qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			queue = qSession.createQueue("test");
			qSender = qSession.createSender(queue);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(String text) {
		try {
			TextMessage message = qSession.createTextMessage(text);
			qSender.send(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void exit() {
		try {
			if (qConnection != null) {
				qConnection.close();
			}
			if (qSession != null) {
				qSession.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws JMSException {
		QSender sender = new QSender();
		sender.sendMessage("test消息");
		System.out.println("消息已发送");
		sender.exit();
	}

}
