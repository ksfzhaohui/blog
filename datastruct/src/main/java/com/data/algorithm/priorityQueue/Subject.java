package com.data.algorithm.priorityQueue;

public class Subject implements Comparable<Subject> {

	private int type;
	private int result;

	public Subject(int type, int result) {
		this.type = type;
		this.result = result;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Subject [type=" + type + ", result=" + result + "]";
	}

	@Override
	public int compareTo(Subject o) {
		if (o.getResult() > getResult()) {
			return 1;
		} else if (o.getResult() == getResult()) {
			return 0;
		}
		return -1;
	}

}
