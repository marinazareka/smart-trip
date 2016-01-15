package oss.fruct.org.smarttrip.transportkp.data;

public class RouteState {
	// Last processed route request
	private RouteRequest routeRequest;

	public RouteState(RouteRequest routeRequest) {
		this.routeRequest = routeRequest;
	}

	public RouteRequest getRouteRequest() {
		return routeRequest;
	}
}
