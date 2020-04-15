package com.data.algorithm.linked;

/**
 * 链表反转
 * 
 * @author hui.zhao.cfs
 *
 */
public class ReverseLinkedList {

	public static void list(LinkedNode header) {
		while (header != null) {
			System.out.println(header.data);
			header = header.next;
		}
	}

	public static LinkedNode reverse(LinkedNode header) {
		LinkedNode preNode = null;
		LinkedNode nowNode = header;

		while (nowNode != null) {
			LinkedNode nextNode = nowNode.next;
			nowNode.next = preNode;
			preNode = nowNode;
			nowNode = nextNode;
		}
		return preNode;
	}

	static LinkedNode reverseByRecursion(LinkedNode head) {
		if (head == null || head.next == null) {
			return head;
		}

		LinkedNode newHead = reverseByRecursion(head.next);

		head.next.next = head;
		head.next = null;
		return newHead;
	}

	public static void main(String[] args) {
		LinkedNode n1 = new LinkedNode(1);
		LinkedNode n2 = new LinkedNode(2);
		LinkedNode n3 = new LinkedNode(3);

		n1.next = n2;
		n2.next = n3;

		list(n1);
		// list(reverse(n1));
		list(reverseByRecursion(n1));
	}
}

class LinkedNode {
	int data;
	LinkedNode next;

	public LinkedNode(int data) {
		this.data = data;
	}
}
