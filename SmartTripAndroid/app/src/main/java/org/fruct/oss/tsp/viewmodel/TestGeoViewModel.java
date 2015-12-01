package org.fruct.oss.tsp.viewmodel;

import android.os.Handler;
import android.os.Looper;

import org.fruct.oss.tsp.commondatatype.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestGeoViewModel extends AbstractGeoViewModel {
	private List<Point> pointsSource = new ArrayList<>();
	private Random random = new Random();

	private final Handler handler;
	private Runnable pointUpdater = new Runnable() {
		@Override
		public void run() {
			refreshPoints();
			if (pointsSource.size() < 10) {
				handler.postDelayed(pointUpdater, 500);
			}
		}
	};

	public TestGeoViewModel() {
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

	private void refreshPoints() {
		double lat = random.nextFloat();
		double lon = random.nextFloat();

		pointsSource.add(new Point("id" + random.nextLong(),
				lat + ":" + lon,
				random.nextFloat(), random.nextFloat()));
		updatePoints(pointsSource);
	}
}
