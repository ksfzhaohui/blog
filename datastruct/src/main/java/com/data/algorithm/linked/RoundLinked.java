package com.data.algorithm.linked;

import java.util.HashSet;

/**
 * 判断是否为环链，入口处
 * 
 * @author hui.zhao.cfs
 *
 */
public class RoundLinked {

	/**
	 * 快慢指针法
	 * 
	 * @param header
	 * @return
	 */
	public static boolean round(RNode header) {
		if (header == null || header.next == null) {
			return false;
		}
		RNode slow = header.next;
		RNode quick = header.next.next;

		while (slow != null && quick != null && slow != quick) {
			slow = slow.next;
			quick = quick.next.next;
		}

		boolean isRound = false;
		if (slow == quick) {
			isRound = true;
		}
		return isRound;
	}

	/**
	 * 节点计数法
	 * 
	 * @param header
	 * @return
	 */
	public static boolean round2(RNode header) {
		HashSet<RNode> set = new HashSet<>();
		while (header != null) {
			if (set.contains(header)) {
				return true;
			} else {
				set.add(header);
			}

			header = header.next;
		}
		return false;
	}

	public static void main(String[] args) {
		RNode n1 = new RNode("1");
		RNode n2 = new RNode("2");
		RNode n3 = new RNode("3");
		RNode n4 = new RNode("4");
		RNode n5 = new RNode("5");
		RNode n6 = new RNode("6");

		n1.next = n2;
		n2.next = n3;
		n3.next = n4;
		n4.next = n5;
		n5.next = n6;
		n6.next = null;

		System.out.println(round(n1));
		System.out.println(round2(n1));
	}
}

class RNode {
	String value;
	RNode next;

	public RNode(String value) {
		this.value = value;
	}
}
