package com.data.algorithm.tree;

import java.util.ArrayList;
import java.util.List;

public class ManyNodeTree {

	// 遍历多叉树
	public void iteratorTree(ManyTreeNode manyTreeNode) {
		if (manyTreeNode != null) {
			System.out.println(manyTreeNode.getData());
			for (ManyTreeNode index : manyTreeNode.getChildList()) {
				if (index.getChildList() != null && index.getChildList().size() > 0) {
					iteratorTree(index);
				}else {
					System.out.println(index.getData());
				}
			}
		}
	}
	
	private static List<ManyTreeNode> pre(ManyTreeNode manyTreeNode) {
		if (manyTreeNode == null) {
			return new ArrayList<>();
		}
		List<ManyTreeNode> list = new ArrayList<ManyTreeNode>();
		list.add(manyTreeNode);
		for (ManyTreeNode index : manyTreeNode.getChildList()) {
			list.addAll(pre(index));
		}
		return list;
	}

	public static void main(String[] args) {
		ManyTreeNode m1 = new ManyTreeNode("系统权限管理");
		ManyTreeNode m21 = new ManyTreeNode("用户管理");
		ManyTreeNode m22 = new ManyTreeNode("角色管理");
		ManyTreeNode m23 = new ManyTreeNode("组管理");
		ManyTreeNode m211 = new ManyTreeNode("增加");
		ManyTreeNode m212 = new ManyTreeNode("删除");
		ManyTreeNode m213 = new ManyTreeNode("更新");

		List<ManyTreeNode> m1List = m1.getChildList();
		m1List.add(m21);
		m1List.add(m22);
		m1List.add(m23);

		List<ManyTreeNode> m21List = m21.getChildList();
		m21List.add(m211);
		m21List.add(m212);
		m21List.add(m213);

		//new ManyNodeTree().iteratorTree(m1);
		List<ManyTreeNode> list = pre(m1);
		for(ManyTreeNode node:list) {
			System.out.println(node.getData());
		}
	}

}

class ManyTreeNode {
	private String data;
	private ManyTreeNode parent;
	private List<ManyTreeNode> childList = new ArrayList<ManyTreeNode>();

	public ManyTreeNode(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public ManyTreeNode getParent() {
		return parent;
	}

	public void setParent(ManyTreeNode parent) {
		this.parent = parent;
	}

	public List<ManyTreeNode> getChildList() {
		return childList;
	}

	public void setChildList(List<ManyTreeNode> childList) {
		this.childList = childList;
	}

}
