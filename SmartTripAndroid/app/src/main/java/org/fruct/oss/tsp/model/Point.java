package org.fruct.oss.tsp.model;

public class Point {
	private String id;
	private double lat;
	private double lon;

	public Point(String id, double lat, double lon) {
		this.id = id;
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
}
