package com.data.algorithm;

/**
 * 求根号n
 * 
 * @author ksfzhaohui
 *
 */
public class Sqrt {

	public static double sqrt2(double num) {
		double low = 0;
		double high = num;
		double precision = 0.000001;

		double mid;
		while (low <= high) {
			mid = low + (high - low) / 2;
			if (Math.abs(num - mid * mid) <= precision) {
				return mid;
			} else {
				if (num > mid * mid) {
					low = mid;
				} else {
					high = mid;
				}
			}
		}
		return high;

	}

	public static double sqrt3(double value, double low, double high, double precision) {
		double mid = low + (high - low) / 2;
		if (Math.abs(value - mid * mid) <= precision) {
			return mid;
		} else {
			if (value > mid * mid) {
				return sqrt3(value, mid, high, precision);
			} else {
				return sqrt3(value, low, mid, precision);
			}
		}
	}

	public static void main(String[] args) {
		System.out.println(Math.sqrt(8));
		System.out.println(sqrt2(8));
		System.out.println(sqrt3(8, 0, 8, 0.000001));
	}
}
