package oss.fruct.org.smarttrip.transportkp;

import oss.fruct.org.smarttrip.transportkp.data.RouteState;

import java.util.HashMap;

public class RequestCache {
	// user id -> RouteState
	private HashMap<String, RouteState> cache = new HashMap<>();

	public RouteState find(String userId) {
		return cache.get(userId);
	}

	public void insert(String userId, RouteState routeState) {
		cache.put(userId, routeState);
	}
}
