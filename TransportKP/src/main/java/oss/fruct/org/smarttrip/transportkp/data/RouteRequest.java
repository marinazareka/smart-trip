package oss.fruct.org.smarttrip.transportkp.data;

public class RouteRequest {
	private Object tag;
	private Point[] points;
	private Point userPoint;
	private String tspType;

	public RouteRequest(Object tag, Point[] points, Point userPoint, String tspType) {
		this.tag = tag;
		this.points = points;
		this.userPoint = userPoint;
		this.tspType = tspType;
	}

	public Object getTag() {
		return tag;
	}

	public Point[] getPoints() {
		return points;
	}

	public Point getUserPoint() {
		return userPoint;
	}

	public String getTspType() {
		return tspType;
	}
}
