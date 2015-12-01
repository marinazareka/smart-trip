package org.fruct.oss.tsp.smartspace;

import android.os.Handler;
import android.util.Log;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.smartslognative.SmartSpaceNative;

import java.util.ArrayList;
import java.util.List;

public class TestSmartSpaceNative implements SmartSpaceNative {
	private static final String TAG = "TestSmartSpaceNative";

	private boolean isSearchFinished;
	private Handler handler;

	private Listener listener;
	private double lat;
	private double lon;


	public TestSmartSpaceNative() {
		handler = new Handler();
	}

	@Override
	public void initialize(String userId) {
		Log.d(TAG, "initialize() called with: " + "userId = [" + userId + "]");
	}

	@Override
	public void shutdown() {
		handler.removeCallbacksAndMessages(null);

		Log.d(TAG, "shutdown() called with: " + "");
	}

	@Override
	public void updateUserLocation(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		Log.d(TAG, "updateUserLocation() called with: " + "lat = [" + lat + "], lon = [" + lon + "]");
	}

	@Override
	public void postSearchRequest(double radius, String pattern) {
		Log.d(TAG, "postSearchRequest() called with: " + "radius = [" + radius + "], pattern = [" + pattern + "]");

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ArrayList<Point> points = new ArrayList<>();
				for (int i = 0; i < 10; i++) {
					double lat1 = lat + (2 * Math.random() - 1) * 0.01;
					double lon1 = lon + (2 * Math.random() - 1) * 0.01;
					String id = "id" + lat1 + ":" + lon1;
					String title = "title" + lat1+ ":" + lon1;
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
	public void postScheduleRequest(final Point[] points) {
		Log.d(TAG, "postScheduleRequest() called with: " + "points = [" + points + "]");

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				List<Movement> movements = new ArrayList<>();
				for (int i = 1; i < points.length; i++) {
					movements.add(new Movement(points[i - 1], points[i]));
				}

				if (listener != null) {
					listener.onScheduleRequestReady(movements.toArray(new Movement[movements.size()]));
				}
			}
		}, 1000);
	}

	@Override
	public void setListener(Listener listener) {
		this.listener = listener;
		Log.d(TAG, "setListener() called with: " + "listener = [" + listener + "]");
	}
}
