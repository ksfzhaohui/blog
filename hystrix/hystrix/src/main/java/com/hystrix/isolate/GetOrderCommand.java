package com.hystrix.isolate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class GetOrderCommand extends HystrixCommand<String> {

	OrderService orderService = new OrderService();

	public GetOrderCommand(String name) {
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ThreadPoolTestGroup"))
				.andCommandKey(HystrixCommandKey.Factory.asKey("testCommandKey"))
				.andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(name))
				.andCommandPropertiesDefaults(
						HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(20000))
				.andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withMaxQueueSize(5) // 配置队列大小
						.withCoreSize(2) // 配置线程池里的线程数
		));
	}

	@Override
	protected String run() {
		return "Thread name="+Thread.currentThread().getName()+","+orderService.getOrderInfo();
	}

	@Override
	protected String getFallback() {
		return "Thread name="+Thread.currentThread().getName()+",fallback order";
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		List<Future<String>> list = new ArrayList<>();
		System.out.println("Thread name="+Thread.currentThread().getName());
		for (int i = 0; i < 8; i++) {
			Future<String> future = new GetOrderCommand("hystrix-order").queue();
			list.add(future);
		}

		for (Future<String> future : list) {
			System.out.println(future.get());
		}

		Thread.sleep(1000000);
	}
}