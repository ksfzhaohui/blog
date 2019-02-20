package com.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * 同步发送
 * 
 * @author hui.zhao.cfs
 *
 */
public class OneWayProducer {

	public static void main(String[] args) throws Exception {

		DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName2");
		producer.setNamesrvAddr("192.168.237.128:9876");
		producer.start();
		for (int i = 0; i < 1; i++) {
			Message msg = new Message("TopicTest6", "TagA",
					("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
			producer.sendOneway(msg);
		}
		producer.shutdown();
	}
}
