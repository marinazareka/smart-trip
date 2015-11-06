package oss.fruct.org.smarttrip.transportkp.data;

public class RouteResponse {
	private Point[] route;
	private Object tag;

	public RouteResponse(Point[] route, Object tag) {
		this.route = route;
		this.tag = tag;
	}

	public Point[] getRoute() {
		return route;
	}

	public Object getRequestTag() {
		return tag;
	}
}
