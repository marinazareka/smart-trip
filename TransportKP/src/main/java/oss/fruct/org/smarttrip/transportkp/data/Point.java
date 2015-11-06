package oss.fruct.org.smarttrip.transportkp.data;

public class Point {
	private double lat, lon;

	public Point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	@Override
	public String toString() {
		return "Point{" +
				"lat=" + lat +
				", lon=" + lon +
				'}';
	}
}
