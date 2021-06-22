package com.zh.timeout;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class FutureTimeout {
	public static void main(String[] args) throws Exception {
//		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
//
//			@Override
//			public String call() throws Exception {
//				return "test";
//			}
//		});
//
//		task.get(1000, TimeUnit.MILLISECONDS);
		
		
		ExecutorService threadPool=Executors.newSingleThreadExecutor();
		Future<String> future=threadPool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				Thread.sleep(2000);
				return "hello";
			}
		});
		System.out.println(future.get(1000, TimeUnit.MILLISECONDS));
		threadPool.shutdown();
	}
}
