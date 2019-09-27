package com.data.algorithm.redPackage;

import java.util.Random;

/**
 * 微信抢红包
 * @author hui.zhao.cfs
 *
 */
public class Test {

	public static void main(String[] args) {
		RedPackage redPackage = new RedPackage(10, 100);
		for (int i = 0; i < 10; i++) {
			System.out.println(getRandomMoney(redPackage));
		}
	}

	public static double getRandomMoney(RedPackage redPackage) {
		if (redPackage.getRemainSize() == 1) {
			redPackage.setRemainSize(redPackage.getRemainSize() - 1);
			return Math.round(redPackage.getRemainMoney() * 100) / 100;
		}
		Random r = new Random();
		double min = 0.01;
		double max = redPackage.getRemainMoney() / redPackage.getRemainSize() * 2;

		double money = r.nextDouble() * max;
		money = money < min ? min : money;
		// 一个表示小于或等于指定数字的最大整数的数字(向下取整)
		money = Math.floor(money * 100) / 100;

		redPackage.setRemainSize(redPackage.getRemainSize() - 1);
		redPackage.setRemainMoney(redPackage.getRemainMoney() - money);
		return money;
	}
}
