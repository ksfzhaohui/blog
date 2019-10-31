package zh.maven.DynamicConf;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicPublisher {
	private static final String TOPIC = "dynamicConfTopic";
	private static final String IDENTIFIER = "confKey";

	public static void main(String[] args) throws JMSException {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		Connection connection = factory.createConnection();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(TOPIC);

		MessageProducer producer = session.createProducer(topic);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		int i=1;
		while (true) {
			TextMessage message = session.createTextMessage();
			message.setStringProperty(IDENTIFIER, "/a2/"+i);
			message.setText("message_" + System.currentTimeMillis());
			producer.send(message);
			System.out.println("Sent message: " + message.getText());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
	}
}
