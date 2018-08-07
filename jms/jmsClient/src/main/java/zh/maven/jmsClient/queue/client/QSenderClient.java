package zh.maven.jmsClient.queue.client;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class QSenderClient {

	private QueueConnectionFactory factory;
	private QueueConnection qConnection;
	private QueueSession qSession;
	private Queue queue;
	private QueueSender qSender;

	public QSenderClient() {
		try {
			factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			qConnection = factory.createQueueConnection();
			qConnection.start();

			qSession = qConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
			queue = qSession.createQueue("test");
			qSender = qSession.createSender(queue);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("初始化生产者失败");
		}
	}

	private void sendMessage(String text) {
		try {
			TextMessage message = qSession.createTextMessage(text);
			qSender.send(message);
			for (int i = 0; i < 10; i++) {
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("发送消息失败，生产者做重发处理");
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

	public static void main(String[] args) throws Exception {
		QSenderClient sender = new QSenderClient();
		String message = "test消息";
		System.out.println("准备发送消息：" + message);
		sender.sendMessage(message);
		System.out.println("消息已发送");
		sender.exit();
	}

}
