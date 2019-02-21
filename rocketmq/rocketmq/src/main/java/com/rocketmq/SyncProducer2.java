package com.rocketmq;

import java.util.Date;
import java.util.List;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * 同步发送--MessageQueueSelector
 * 
 * @author hui.zhao.cfs
 *
 */
public class SyncProducer2 {

	public static void main(String[] args) throws Exception {

		System.setProperty("rocketmq.namesrv.domain", "localhost");
		// 构造Producer
		DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName2");
		producer.start();

		for (int i = 0; i < 5; i++) {
			Message msg = new Message("TopicTest1234", "TagA",
					("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
			SendResult sendResult = producer.send(msg, new MessageQueueSelector() {

				@Override
				public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
					return mqs.get(0);
				}
			}, i);
			System.out.println("Time [" + new Date().toString() + "]," + sendResult);
		}
		producer.shutdown();
	}
}
