package com.data.algorithm.redPackage;

public class RedPackage {

	private int remainSize;
	private double remainMoney;

	public RedPackage(int remainSize, double remainMoney) {
		this.remainMoney = remainMoney;
		this.remainSize = remainSize;
	}

	public int getRemainSize() {
		return remainSize;
	}

	public void setRemainSize(int remainSize) {
		this.remainSize = remainSize;
	}

	public double getRemainMoney() {
		return remainMoney;
	}

	public void setRemainMoney(double remainMoney) {
		this.remainMoney = remainMoney;
	}

	@Override
	public String toString() {
		return "RedPackage [remainSize=" + remainSize + ", remainMoney=" + remainMoney + "]";
	}

}
