package oss.fruct.org.smarttrip.transportkp.smartspace;

import oss.fruct.org.smarttrip.transportkp.data.RouteRequest;
import oss.fruct.org.smarttrip.transportkp.data.RouteResponse;

public interface SmartSpace {
	boolean init();
	void shutdown();

	boolean subscribe();
	void unsubscribe();

	RouteRequest waitSubscription();
	void publish(RouteResponse response);
}
