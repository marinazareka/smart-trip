package org.fruct.oss.tsp.data;

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

	public String getId() {
		return id;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public String getTitle() {
		return title;
	}
}
