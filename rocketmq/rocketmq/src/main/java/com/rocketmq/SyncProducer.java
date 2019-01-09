package com.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

public class SyncProducer {

	public static void main(String[] args) throws Exception {
		// 构造Producer
		DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName");
		producer.setNamesrvAddr("192.168.237.128:9876");
		// 初始化Producer，整个应用生命周期内，只需要初始化1次
		producer.start();
		for (int i = 0; i < 100; i++) {
			Message msg = new Message("TopicTest", "TagA",
					("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
			SendResult sendResult = producer.send(msg);
			System.out.println(sendResult);
		}
		producer.shutdown();
	}
}
