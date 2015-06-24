package org.fruct.oss.smarttrip.points;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import org.fruct.oss.smarttrip.App;
import org.fruct.oss.smarttrip.SmartService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Loads points from smartspace.
 */
public class SmartPointsLoader implements PointsLoader {
	private List<Point> resultPoints;
	private CountDownLatch latch = new CountDownLatch(1);
	private Messenger receiver;

	@Override
	public List<Point> loadPoints(final double latCenter, final double lonCenter, final double radius) {
		Handler handler = new ResponseHandler();
		receiver = new Messenger(handler);

		handler.post(new Runnable() {
			@Override
			public void run() {
				startLoadPoints(latCenter, lonCenter, radius);
			}
		});

		try {
			latch.await();
		} catch (InterruptedException ignored) {
			return null;
		}

		return resultPoints;
	}

	private void startLoadPoints(double latCenter, double lonCenter, double radius) {
		Context context = App.getContext();
		Intent intent = new Intent(SmartService.ACTION_QUERY_POINTS, null, context, SmartService.class);
		intent.putExtra(SmartService.EXTRA_LAT, latCenter);
		intent.putExtra(SmartService.EXTRA_LON, lonCenter);
		intent.putExtra(SmartService.EXTRA_RADIUS, radius);
		intent.putExtra(SmartService.EXTRA_REPLY_TO, receiver);
		context.startService(intent);
	}

	private void onPointsLoaded(List<Point> points) {
		resultPoints = points;
		latch.countDown();
	}

	private class ResponseHandler extends Handler {
		public ResponseHandler() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SmartService.RESPONSE_POINTS) {
				msg.getData().setClassLoader(getClass().getClassLoader());

				List<Point> points = msg.getData().getParcelableArrayList(SmartService.RESPONSE_EXTRA_POINTS);
				onPointsLoaded(points);
			}
		}
	}
}
