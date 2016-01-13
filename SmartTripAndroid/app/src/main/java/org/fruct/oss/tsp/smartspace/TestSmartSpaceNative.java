package org.fruct.oss.tsp.smartspace;

import android.os.Handler;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.SmartSpaceNative;
import org.fruct.oss.tsp.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TestSmartSpaceNative implements SmartSpaceNative {
	private static final Logger log = LoggerFactory.getLogger(TestSmartSpaceNative.class);

	private boolean isSearchFinished;
	private Handler handler;

	private SmartSpaceNative.Listener listener;
	private double lat;
	private double lon;


	public TestSmartSpaceNative() {
		handler = new Handler();
	}

	@Override
	public void initialize(String userId, String kpName, String smartSpaceName, String address, int port) {
		log.debug("initialize() called with: " + "userId = [" + userId + "], kpName = [" + kpName + "], smartSpaceName = [" + smartSpaceName + "], address = [" + address + "], port = [" + port + "]");
	}

	@Override
	public void shutdown() {
		handler.removeCallbacksAndMessages(null);

		log.debug("shutdown() called with: " + "");
	}

	@Override
	public void updateUserLocation(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		log.debug("updateUserLocation() called with: " + "lat = [" + lat + "], lon = [" + lon + "]");
	}

	@Override
	public void postSearchRequest(double radius, String pattern) {
		log.debug("postSearchRequest() called with: " + "radius = [" + radius + "], pattern = [" + pattern + "]");

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ArrayList<Point> points = new ArrayList<>();
				for (int i = 0; i < 10; i++) {
					double lat1 = lat + (2 * Math.random() - 1) * 0.01;
					double lon1 = lon + (2 * Math.random() - 1) * 0.01;
					String id = "id" + lat1 + ":" + lon1;
					String title = Utils.randomName();
					points.add(new Point(id, title, lat1, lon1));
				}

				if (listener != null) {
					listener.onSearchRequestReady(points.toArray(new Point[points.size()]));
				}

				isSearchFinished = true;
			}
		}, 1000);
	}

	@Override
	public void postScheduleRequest(final Point[] points, final String tspType) {
		log.debug("postScheduleRequest() called with: " + "points = [" + points + "]");

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				List<Movement> movements = new ArrayList<>();
				movements.add(new Movement(new Point("user", "Untitled", lat, lon), points[0]));

				for (int i = 1; i < points.length; i++) {
					movements.add(new Movement(points[i - 1], points[i]));
				}

				if (tspType.equals("CLOSED")) {
					movements.add(new Movement(points[points.length - 1],
							new Point("user", "Untitled", lat, lon)));
				}

				if (listener != null) {
					listener.onScheduleRequestReady(movements.toArray(new Movement[movements.size()]));
				}


				//NativeTest.divide(5, movements.size() - 1);
			}
		}, 1000);
	}

	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
		log.debug("setListener() called with: " + "listener = [" + listener + "]");
	}
}
