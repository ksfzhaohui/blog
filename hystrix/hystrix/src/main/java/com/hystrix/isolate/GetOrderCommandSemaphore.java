package com.hystrix.isolate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

public class GetOrderCommandSemaphore extends HystrixCommand<String> {

	OrderService orderService = new OrderService();

	public GetOrderCommandSemaphore(String name) {
		// 设置信号量隔离策略
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetCityNameGroup"))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
						.withExecutionIsolationSemaphoreMaxConcurrentRequests(8)));
	}

	@Override
	protected String run() throws Exception {
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
			Future<String> future = new GetOrderCommandSemaphore("hystrix-order").queue();
			list.add(future);
		}

		for (Future<String> future : list) {
			System.out.println(future.get());
		}

		Thread.sleep(1000000);
	}
}