package com.data.algorithm.leetcode;

public class 合并两个有序链表 {

	public static class ListNode {
		int val;
		ListNode next;

		ListNode(int x) {
			val = x;
		}
	}

	public static ListNode mergeTwoLists(ListNode l1, ListNode l2) {
		ListNode first = new ListNode(0);
		ListNode head = first;
		while (l1 != null && l2 != null) {
			if (l1.val < l2.val) {
				ListNode ln = new ListNode(l1.val);
				first.next = ln;
				first = ln;
				l1 = l1.next;
			} else {
				ListNode ln = new ListNode(l2.val);
				first.next = ln;
				first = ln;
				l2 = l2.next;
			}
		}

		while (l1 != null) {
			ListNode ln = new ListNode(l1.val);
			first.next = ln;
			l1 = l1.next;
		}
		while (l2 != null) {
			ListNode ln = new ListNode(l2.val);
			first.next = ln;
			l2 = l2.next;
		}

		return head = head.next;
	}

	public static void main(String[] args) {
		ListNode l11 = new ListNode(1);
		ListNode l12 = new ListNode(2);
		ListNode l13 = new ListNode(4);
		l11.next = l12;
		l12.next = l13;

		ListNode l21 = new ListNode(1);
		ListNode l22 = new ListNode(3);
		ListNode l23 = new ListNode(4);
		l21.next = l22;
		l22.next = l23;

		System.out.println(print(l11));
		System.out.println(print(l21));
		System.out.println(print(mergeTwoLists(l11, l21)));
	}

	private static String print(ListNode root) {
		StringBuffer sb = new StringBuffer();
		sb.append(root.val);
		while (root.next != null) {
			sb.append(root.next.val);
			root = root.next;
		}
		return sb.toString();
	}
}
