package oss.fruct.org.smarttrip.transportkp.data;

public class RouteRequest {
	private Object tag;
	private Point[] points;

	public RouteRequest(Object tag, Point[] points) {
		this.tag = tag;
		this.points = points;
	}

	public Object getId() {
		return tag;
	}

	public Point[] getPoints() {
		return points;
	}
}
