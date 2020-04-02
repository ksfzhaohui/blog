package com.data.algorithm.priorityQueue;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 通过堆实现，具体说是通过完全二叉树（complete binary tree）实现的小顶堆（任意一个非叶子节点的权值，都不大于其左右子节点的权值），
 * 也就意味着可以通过数组来作为 PriorityQueue 的底层实现。
 * 
 * @author hui.zhao.cfs
 *
 */
public class PriorityQueueTest {

	public static void main(String[] args) {

		Queue<String> q = new PriorityQueue<>();
		q.offer("apple");
		q.offer("pear");
		q.offer("banana");
		System.out.println(q.poll()); // apple
		System.out.println(q.poll()); // banana
		System.out.println(q.poll()); // pear
		System.out.println(q.poll()); // null,因为队列为空

		PriorityQueue<Subject> priorityQueue = new PriorityQueue<Subject>();
		priorityQueue.offer(new Subject(1, 88));
		priorityQueue.offer(new Subject(1, 87));
		priorityQueue.offer(new Subject(1, 90));

		System.out.println(priorityQueue.poll());
		System.out.println(priorityQueue.poll());
		System.out.println(priorityQueue.poll());
	}

}
