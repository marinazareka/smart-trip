package org.fruct.oss.tsp.layers;

import android.content.Context;
import android.location.Location;

import org.fruct.oss.tsp.util.LocationProvider;
import org.fruct.oss.tsp.util.Utils;
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

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class UserLayer extends Layer {
	private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;

	private final Paint circleFill;
	private final Paint circleStroke;
	private final int circleRadius;
	private final Context context;

	private FixedPixelCircle circle;
	private Subscription locationSubscription;

	public UserLayer(Context context) {
		this.context = context;
		circleFill = Utils.createPaint(GRAPHIC_FACTORY.createColor(200, 200, 50, 60), 0, Style.FILL);
		circleStroke = Utils.createPaint(GRAPHIC_FACTORY.createColor(240, 200, 50, 60), Utils.getDP(1), Style.STROKE);
		circleRadius = Utils.getDP(3);

	}

	@Override
	protected void onAdd() {
		super.onAdd();
		locationSubscription = LocationProvider.getObservable(context, LocationProvider.REQUEST_MAP)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<Location>() {
					@Override
					public void call(Location location) {
						updateLocation(location);
					}
				});

	}

	@Override
	protected void onRemove() {
		locationSubscription.unsubscribe();
		super.onRemove();
	}

	@Override
	public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		if (circle == null) {
			return;
		}

		circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
	}

	private void updateLocation(Location location) {
		LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude());
		if (circle == null) {
			circle = new FixedPixelCircle(latLong, circleRadius, circleFill, circleStroke);
			circle.setDisplayModel(displayModel);
		} else {
			circle.setLatLong(latLong);
		}
		requestRedraw();
	}
}