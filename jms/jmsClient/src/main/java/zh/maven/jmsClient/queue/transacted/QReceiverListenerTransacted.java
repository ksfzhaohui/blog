package zh.maven.jmsClient.queue.transacted;

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

public class QReceiverListenerTransacted implements MessageListener {

	private QueueConnectionFactory factory;
	private QueueConnection qConnection;
	private QueueSession qSession;
	private Queue queue;
	private QueueReceiver qReceiver;

	public QReceiverListenerTransacted() {
		try {
			factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			qConnection = factory.createQueueConnection();
			qConnection.start();

			qSession = qConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
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
			if (textMessage.getText().equals("end")) {
				qSession.commit();
			}
		} catch (JMSException e) {
			try {
				qSession.rollback();
			} catch (JMSException e1) {
			}
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new QReceiverListenerTransacted();
	}

}
