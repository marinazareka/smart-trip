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

	// Intellij Idea generated
	@Override
	public String toString() {
		return "Point{" +
				"id=" + id +
				", lat=" + lat +
				", lon=" + lon +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Point point = (Point) o;

		if (Double.compare(point.lat, lat) != 0) return false;
		return Double.compare(point.lon, lon) == 0;

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
