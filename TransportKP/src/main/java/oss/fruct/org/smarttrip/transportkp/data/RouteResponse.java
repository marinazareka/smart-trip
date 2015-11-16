package oss.fruct.org.smarttrip.transportkp.data;

public class RouteResponse {
	private Point[] route;
	private String roadType;
	private Object tag;

	public RouteResponse(Point[] route, String roadType, Object tag) {
		this.route = route;
		this.roadType = roadType;
		this.tag = tag;
	}

	public Point[] getRoute() {
		return route;
	}

	public Object getRequestTag() {
		return tag;
	}

	public String getRoadType() {
		return roadType;
	}
}
