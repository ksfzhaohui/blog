package com.netty.timer;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTaskTest {

	public static void main(String[] args) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println(System.currentTimeMillis() + "  === task1");
			}
		}, 1000, 1000);

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				System.out.println(System.currentTimeMillis() + "  === task2");
			}
		}, 1000, 1000);
	}

}
