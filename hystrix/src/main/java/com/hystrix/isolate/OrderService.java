package com.hystrix.isolate;

public class OrderService {

	public String getOrderInfo() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "orderId=" + System.currentTimeMillis();
	}
}
