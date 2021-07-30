package com.data.algorithm.lru;

import java.util.HashMap;
import java.util.Map;

/**
 * 双链表 + 哈希表实现lru缓存策略
 * 
 * @author hui.zhao.cfs
 *
 */
public class LRUCache {

	private Map<String, LRUNode> map = new HashMap<String, LRUNode>();

	/** 容量 **/
	private int capacity;

	private LRUNode head;
	private LRUNode tail;

	public LRUCache(int capacity) {
		this.capacity = capacity;
	}

	public void put(String key, Object value) {
		if (head == null) {
			LRUNode node = new LRUNode(key, value);
			head = node;
			tail = node;
			map.put(key, node);
		}

		LRUNode node = map.get(key);
		if (node == null) {
			node = new LRUNode(key, value);
			if (map.size() >= capacity) {
				map.remove(tail);
				tail = tail.pre;
				tail.next = null;
			}
			map.put(key, node);

			node.next = head;
			head.pre = node;
			head = node;
		} else {
			node.value = value;
			removeAndInsert(node);
		}

	}

	public Object get(String key) {
		LRUNode node = map.get(key);
		if (node != null) {
			// 把这个节点删除并插入到头结点
			removeAndInsert(node);
			return node.value;
		} else {
			return null;
		}
	}

	public void removeAndInsert(LRUNode node) {
		// 特殊情况先判断，例如该节点是头结点或是尾部节点
		if (node == head) {
			return;
		} else if (node == tail) {
			tail = node.pre;
			tail.next = null;
		} else {
			node.pre.next = node.next;
			node.next.pre = node.pre;
		}
		// 插入到头结点
		node.next = head;
		head.pre = node;
		node.pre = null;

		head = node;
	}

}

class LRUNode {

	String key;
	Object value;
	LRUNode pre;
	LRUNode next;

	public LRUNode(String key, Object value) {
		this.key = key;
		this.value = value;
	}

}