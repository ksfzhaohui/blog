package com.rocketmq;

import java.util.Date;
import java.util.List;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

public class PushConsumer2 {

	public static void main(String[] args) throws MQClientException {

		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("ConsumerGroupName");
		consumer.setNamesrvAddr("192.168.237.128:9876");
		consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
		consumer.subscribe("TopicTest1234", "*");
		
		consumer.registerMessageListener(new MessageListenerOrderly() {
			
			@Override
			public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
				System.out.printf("Time [" + new Date().toString() + "]," +Thread.currentThread().getName() + "Receive New Messages :" + msgs + "%n");
				return ConsumeOrderlyStatus.SUCCESS;
			}
		});
		consumer.start();
	}

}
