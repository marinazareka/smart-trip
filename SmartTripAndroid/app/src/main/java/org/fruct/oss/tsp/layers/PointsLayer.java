package org.fruct.oss.tsp.layers;

import android.content.Context;

import com.google.android.gms.maps.model.Polyline;

import org.fruct.oss.tsp.util.Utils;
import org.fruct.oss.tsp.viewmodel.GeoViewModel;
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

public class PointsLayer extends Layer implements GeoViewModel.Listener {
	private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;

	private final Context context;
	private GeoViewModel geoViewModel;

	private final Paint circleFill;
	private final Paint circleFillChecked;
	private final Paint circleStroke;

	private final int radius;

	private List<PointLayer> pointLayers = new ArrayList<>();

	private byte lastZoom;

	public PointsLayer(Context context, GeoViewModel geoViewModel) {
		this.context = context;

		this.geoViewModel = geoViewModel;

		circleFill = Utils.createPaint(GRAPHIC_FACTORY.createColor(170, 100, 110, 255), 0, Style.FILL);
		circleFillChecked = Utils.createPaint(GRAPHIC_FACTORY.createColor(170, 100, 200, 255), 0, Style.FILL);

		circleStroke = Utils.createPaint(GRAPHIC_FACTORY.createColor(200, 100, 110, 210), 2, Style.STROKE);
		radius = Utils.getDP(8);
	}

	@Override
	public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		lastZoom = zoomLevel;

		for (PointLayer pointLayer : pointLayers) {
			pointLayer.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
		}
	}

	@Override
	protected void onAdd() {
		super.onAdd();
		geoViewModel.registerListener(this);

		//pointsUpdated(geoViewModel.getPoints());
	}

	@Override
	protected void onRemove() {
		geoViewModel.unregisterListener(this);
		super.onRemove();
	}

	@Override
	public void pointsUpdated(List<GeoViewModel.PointModel> points) {
		pointLayers.clear();
		int c = 0;
		for (GeoViewModel.PointModel point : points) {
			pointLayers.add(new PointLayer(c++, point));
		}
	}

	@Override
	public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
		long mapSize = MercatorProjection.getMapSize((byte) lastZoom, displayModel.getTileSize());

		double tx = MercatorProjection.longitudeToPixelX(tapLatLong.longitude, mapSize);
		double ty = MercatorProjection.latitudeToPixelY(tapLatLong.latitude, mapSize);

		for (PointLayer pointHolder : pointLayers) {
			double x = MercatorProjection.longitudeToPixelX(pointHolder.pointModel.point.getLon(), mapSize);
			double y = MercatorProjection.latitudeToPixelY(pointHolder.pointModel.point.getLat(), mapSize);

			double dx = x - tx;
			double dy = y - ty;

			if (dx * dx + dy * dy < radius * radius) {
				onPointClicked(pointHolder);
				return true;
			}
		}

		return super.onTap(tapLatLong, layerXY, tapXY);
	}

	private void onPointClicked(PointLayer pointLayer) {
		geoViewModel.setCheckedState(pointLayer.index, !pointLayer.pointModel.isChecked);
		pointLayer.updateCheckedState();
		requestRedraw();
	}

	private class PointLayer extends FixedPixelCircle {
		private int index;
		private GeoViewModel.PointModel pointModel;

		public PointLayer(int index, GeoViewModel.PointModel pointModel) {
			super(new LatLong(pointModel.point.getLat(), pointModel.point.getLon()),
					radius, circleFill, circleStroke);
			setDisplayModel(PointsLayer.this.displayModel);

			this.index = index;
			this.pointModel = pointModel;

			updateCheckedState();
		}

		public void updateCheckedState() {
			if (pointModel.isChecked) {
				setPaintFill(circleFillChecked);
			} else {
				setPaintFill(circleFill);
			}
		}
	}
}
