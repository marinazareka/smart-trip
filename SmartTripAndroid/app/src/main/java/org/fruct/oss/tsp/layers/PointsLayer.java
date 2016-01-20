package org.fruct.oss.tsp.layers;

import android.content.Context;

import org.fruct.oss.tsp.database.DatabaseRepo;
import org.fruct.oss.tsp.stores.SearchStore;
import org.fruct.oss.tsp.util.Utils;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.fruct.oss.tsp.commondatatype.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;

public class PointsLayer extends Layer {
	private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;

	private final Context context;
	private final DatabaseRepo repo;
	private final SearchStore searchStore;

	private final Paint circleFill;
	private final Paint circleFillChecked;
	private final Paint circleStroke;

	private final int radius;

	private List<PointLayer> pointLayers = new ArrayList<>();

	private byte lastZoom;
	private Subscription subscription;

	public PointsLayer(Context context, DatabaseRepo repo, SearchStore searchStore) {
		this.context = context;

		this.repo = repo;
		this.searchStore = searchStore;

		radius = Utils.getDP(8);

		circleFill = Utils.createPaint(GRAPHIC_FACTORY.createColor(170, 100, 110, 255), 0, Style.FILL);
		circleFillChecked = Utils.createPaint(GRAPHIC_FACTORY.createColor(170, 100, 200, 255), 0, Style.FILL);

		circleStroke = Utils.createPaint(GRAPHIC_FACTORY.createColor(200, 100, 110, 210), Utils.getDP(2), Style.STROKE);
	}


	@Override
	public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, org.mapsforge.core.model.Point topLeftPoint) {
		lastZoom = zoomLevel;

		for (PointLayer pointLayer : pointLayers) {
			pointLayer.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
		}
	}

	@Override
	protected void onAdd() {
		super.onAdd();

		subscription = Observable.combineLatest(searchStore.getObservable(), repo.loadCurrentSchedulePoints(),
				new Func2<List<Point>, List<Point>, List<Point>>() {
					@Override
					public List<Point> call(List<Point> searchedPoints, List<Point> schedulePoints) {
						return searchedPoints.isEmpty() ? schedulePoints : searchedPoints;
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<Point>>() {
					@Override
					public void call(List<Point> points) {
						updatePoints(points);
					}
				});
	}

	@Override
	protected void onRemove() {
		subscription.unsubscribe();
		super.onRemove();
	}

	public synchronized void updatePoints(List<Point> points) {
		pointLayers.clear();
		int c = 0;
		for (Point point : points) {
			pointLayers.add(new PointLayer(c++, point));
		}
		requestRedraw();
	}

	@Override
	public boolean onTap(LatLong tapLatLong, org.mapsforge.core.model.Point layerXY,
						 org.mapsforge.core.model.Point tapXY) {
		long mapSize = MercatorProjection.getMapSize((byte) lastZoom, displayModel.getTileSize());

		double tx = MercatorProjection.longitudeToPixelX(tapLatLong.longitude, mapSize);
		double ty = MercatorProjection.latitudeToPixelY(tapLatLong.latitude, mapSize);

		for (PointLayer pointLayer : pointLayers) {
			double x = MercatorProjection.longitudeToPixelX(pointLayer.point.getLon(), mapSize);
			double y = MercatorProjection.latitudeToPixelY(pointLayer.point.getLat(), mapSize);

			double dx = x - tx;
			double dy = y - ty;

			if (dx * dx + dy * dy < radius * radius) {
				// TODO: handle tap somehow
				//onPointClicked(pointLayer);
				return true;
			}
		}

		return super.onTap(tapLatLong, layerXY, tapXY);
	}

	private class PointLayer extends FixedPixelCircle {
		private int index;
		private Point point;

		public PointLayer(int index, Point point) {
			super(new LatLong(point.getLat(), point.getLon()),
					radius, circleFill, circleStroke);
			setDisplayModel(PointsLayer.this.displayModel);

			this.index = index;
			this.point = point;

		}
	}
}
