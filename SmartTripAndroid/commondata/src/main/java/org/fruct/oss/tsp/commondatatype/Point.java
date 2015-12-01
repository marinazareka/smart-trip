package org.fruct.oss.tsp.commondatatype;

/**
 * Информация о географическом объекте
 */
public class Point {
	private String id;
	private String title;
	private double lat;
	private double lon;

	public Point(String id, String title, double lat, double lon) {
		this.id = id;
		this.title = title;
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 *
	 * @return Уникальный идентификатор точки
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Широта
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @return Долгота
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @return Название точки
	 */
	public String getTitle() {
		return title;
	}
}
