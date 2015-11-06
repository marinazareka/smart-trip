package oss.fruct.org.smarttrip.transportkp.smartspace;

import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.data.RouteRequest;
import oss.fruct.org.smarttrip.transportkp.data.RouteResponse;

public class MockSmartSpace implements SmartSpace {
	private int remain = 5;

	@Override
	public boolean init(String name, String smartspace, String address, int port) {
		return true;
	}

	@Override
	public void shutdown() {

	}

	@Override
	public RouteRequest waitSubscription() {
		if (remain <= 0) {
			halt();
		}

		remain -= 1;
		return new RouteRequest("test", createMockPoints());
	}

	@Override
	public void unsubscribe() {
	}

	@Override
	public boolean subscribe() {
		return true;
	}

	private Point[] createMockPoints() {
		Point[] points = new Point[] {
			new Point(61.787351,34.354369),
				new Point(61.792167,34.369475),
				new Point(61.787859,34.375612),
				new Point(61.787026,34.365269),
				new Point(61.783023,34.360334),
		};
		return points;
	}

	private void halt() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	@Override
	public void publish(RouteResponse response) {

	}
}
