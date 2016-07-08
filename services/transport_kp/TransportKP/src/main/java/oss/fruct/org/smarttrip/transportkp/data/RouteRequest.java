package oss.fruct.org.smarttrip.transportkp.data;

public class RouteRequest {
	private Object tag;
	private String userId;
	private Point[] points;
	private Point userPoint;
	private String tspType;
	private String roadType;

	public RouteRequest(Object tag, String userId, Point[] points, Point userPoint, String tspType, String roadType) {
		this.tag = tag;
		this.userId = userId;
		this.points = points;
		this.userPoint = userPoint;
		this.tspType = tspType;
		this.roadType = roadType;
	}

	public boolean isValid() {
		return tag != null && userId != null && points != null && userPoint != null;
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
		if (tspType != null && !tspType.isEmpty())
			return tspType;
		else
			return "open";
	}

	public String getUserId() {
		return userId;
	}

	public String getRoadType() {
		if (roadType != null && !roadType.isEmpty())
			return roadType;
		else
			return "foot";
	}
}
