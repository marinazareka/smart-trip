package org.fruct.oss.smarttrip.layers;

import android.content.Context;
import android.util.Log;

import org.fruct.oss.smarttrip.EventReceiver;
import org.fruct.oss.smarttrip.events.PointsLoadedEvent;
import org.fruct.oss.smarttrip.util.Utils;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class PointsLayer extends Layer {
	private static final String TAG = "PointsLayer";

	private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;

	private final Context context;

	private final Paint circleFill;
	private final Paint circleStroke;

	private List<PointHolder> pointHolders = new ArrayList<>();

	public PointsLayer(Context context) {
		this.context = context;
		circleFill = Utils.createPaint(GRAPHIC_FACTORY.createColor(127, 100, 200, 255), 0, Style.FILL);
		circleStroke = Utils.createPaint(GRAPHIC_FACTORY.createColor(200, 100, 255, 210), 2, Style.STROKE);
	}

	@Override
	protected void onAdd() {
		super.onAdd();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onRemove() {
		EventBus.getDefault().unregister(this);
		super.onRemove();
	}

	@EventReceiver
	public void onEventMainThread(PointsLoadedEvent pointsLoadedEvent) {
		Log.d(TAG, "New points received");
		List<org.fruct.oss.smarttrip.points.Point> points = pointsLoadedEvent.getPoints();
		updatePoints(points);
	}

	private void updatePoints(List<org.fruct.oss.smarttrip.points.Point> points) {
		// This is main thread, so it is safe to clear array
		pointHolders.clear();
		for (org.fruct.oss.smarttrip.points.Point point : points) {
			LatLong latLong = new LatLong(point.getLatitude(), point.getLongitude());

			PointHolder pointHolder = new PointHolder(
					new FixedPixelCircle(latLong, Utils.getDP(8), circleFill, circleStroke), point);
			pointHolder.circle.setDisplayModel(displayModel);

			pointHolders.add(pointHolder);
		}
		requestRedraw();
	}

	@Override
	public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		for (PointHolder pointHolder : pointHolders) {
			pointHolder.circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
		}
	}

	private class PointHolder {
		private FixedPixelCircle circle;
		private org.fruct.oss.smarttrip.points.Point point;

		public PointHolder(FixedPixelCircle circle, org.fruct.oss.smarttrip.points.Point point) {
			this.circle = circle;
			this.point = point;
		}
	}
}
