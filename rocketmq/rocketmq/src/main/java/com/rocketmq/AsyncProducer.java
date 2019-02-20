package com.rocketmq;

import java.io.UnsupportedEncodingException;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * 异步发送
 * 
 * @author hui.zhao.cfs
 *
 */
public class AsyncProducer {

	public static void main(String[] args)
			throws MQClientException, RemotingException, InterruptedException, UnsupportedEncodingException {
		DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName2");
		producer.setRetryTimesWhenSendAsyncFailed(3);
		producer.setNamesrvAddr("192.168.237.128:9876");
		producer.start();
		for (int i = 0; i < 1; i++) {
			Message msg = new Message("TopicTest6", "TagA",
					("Hello RocketMQ" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
			producer.send(msg, new SendCallback() {
				@Override
				public void onSuccess(SendResult sendResult) {
					System.out.println(sendResult);
				}

				@Override
				public void onException(Throwable e) {
					e.printStackTrace();
				}
			});
		}
	}
}
