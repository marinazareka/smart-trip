package org.fruct.oss.tsp.model;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ivashov on 24.11.15.
 */
public class TestTripModel extends AbstractTripModel {
	private List<Point> pointsSource = new ArrayList<>();
	private List<Point> points;

	private final Handler handler;
	private Runnable pointUpdater = new Runnable() {
		@Override
		public void run() {
			refreshPoints();
			handler.postDelayed(pointUpdater, 500);
		}
	};

	public TestTripModel() {
		handler = new Handler(Looper.getMainLooper());
		refreshPoints();
	}

	@Override
	public void start() {
		handler.postDelayed(pointUpdater, 500);
	}

	@Override
	public void stop() {
		handler.removeCallbacks(pointUpdater);
	}

	@Override
	public List<Point> getPoints() {
		return new ArrayList<>(points);
	}

	private void refreshPoints() {
		pointsSource.add(new Point(pointsSource.size(), pointsSource.size()));
		points = Collections.unmodifiableList(new ArrayList<>(pointsSource));
		notifyPointsUpdated(points);
	}
}
