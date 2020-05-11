package com.netty.timer;

import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class HashedWheelTimerTest {

	public static void main(String[] args) throws InterruptedException {
		HashedWheelTimer hashedWheelTimer = new HashedWheelTimer(1000, TimeUnit.MILLISECONDS, 16);
		hashedWheelTimer.newTimeout(new TimerTask() {

			@Override
			public void run(Timeout timeout) throws Exception {
				System.out.println(System.currentTimeMillis() + "  === executed");
			}
		}, 1, TimeUnit.SECONDS);
	}

}
