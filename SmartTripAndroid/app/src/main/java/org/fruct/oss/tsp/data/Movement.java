package org.fruct.oss.tsp.data;

public class Movement {
	private Point a;
	private Point b;

	public Movement(Point a, Point b) {
		this.a = a;
		this.b = b;
	}

	public Point getA() {
		return a;
	}

	public Point getB() {
		return b;
	}
}
