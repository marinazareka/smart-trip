package org.fruct.oss.tsp.commondatatypes;

/**
 * Отрезок пути маршурута
 */
public class Movement {
	private Point a;
	private Point b;

	public Movement(Point a, Point b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * @return Начальная точка маршрута
	 */
	public Point getA() {
		return a;
	}

	/**
	 * @return Конечная точка маршрута
	 */
	public Point getB() {
		return b;
	}
}
