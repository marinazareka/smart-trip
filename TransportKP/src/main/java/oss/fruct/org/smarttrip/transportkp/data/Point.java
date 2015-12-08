package oss.fruct.org.smarttrip.transportkp.data;

public class Point {
	private int id;
	private double lat, lon;

	public Point(int id, double lat, double lon) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Point{" +
				"id=" + id +
				", lat=" + lat +
				", lon=" + lon +
				'}';
	}

}
