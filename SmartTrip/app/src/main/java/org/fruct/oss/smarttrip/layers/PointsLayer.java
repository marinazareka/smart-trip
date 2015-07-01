package org.fruct.oss.smarttrip.layers;

import android.content.Context;
import android.util.Log;

import org.fruct.oss.smarttrip.EventReceiver;
import org.fruct.oss.smarttrip.events.PointClickedEvent;
import org.fruct.oss.smarttrip.events.PointsLoadedEvent;
import org.fruct.oss.smarttrip.util.Utils;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
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
	private int radius;

	private int lastZoom;

	public PointsLayer(Context context) {
		this.context = context;
		circleFill = Utils.createPaint(GRAPHIC_FACTORY.createColor(170, 100, 110, 255), 0, Style.FILL);
		circleStroke = Utils.createPaint(GRAPHIC_FACTORY.createColor(200, 100, 110, 210), 2, Style.STROKE);

		radius = Utils.getDP(8);
	}

	@Override
	protected void onAdd() {
		super.onAdd();
		EventBus.getDefault().registerSticky(this);
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
		// This is not main thread, so update PointHolders list atomically
		List<PointHolder> newPointHolders = new ArrayList<>();
		for (org.fruct.oss.smarttrip.points.Point point : points) {
			LatLong latLong = new LatLong(point.getLatitude(), point.getLongitude());

			PointHolder pointHolder = new PointHolder(
					new FixedPixelCircle(latLong, radius, circleFill, circleStroke), point);
			pointHolder.circle.setDisplayModel(displayModel);

			newPointHolders.add(pointHolder);
		}

		synchronized (PointsLayer.this) {
			pointHolders = newPointHolders;
		}

		requestRedraw();
	}

	@Override
	public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		lastZoom = zoomLevel;

		for (PointHolder pointHolder : pointHolders) {
			pointHolder.circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
		}
	}

	@Override
	public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
		long mapSize = MercatorProjection.getMapSize((byte) lastZoom, displayModel.getTileSize());

		double tx = MercatorProjection.longitudeToPixelX(tapLatLong.longitude, mapSize);
		double ty = MercatorProjection.latitudeToPixelY(tapLatLong.latitude, mapSize);


		for (PointHolder pointHolder : pointHolders) {
			double x = MercatorProjection.longitudeToPixelX(pointHolder.point.getLongitude(), mapSize);
			double y = MercatorProjection.latitudeToPixelY(pointHolder.point.getLatitude(), mapSize);

			double dx = x - tx;
			double dy = y - ty;

			if (dx * dx + dy * dy < radius * radius) {
				EventBus.getDefault().post(new PointClickedEvent(pointHolder.point));
				return true;
			}
		}

		return super.onTap(tapLatLong, layerXY, tapXY);
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
