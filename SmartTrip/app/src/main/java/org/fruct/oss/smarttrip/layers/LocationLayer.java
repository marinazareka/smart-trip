package org.fruct.oss.smarttrip.layers;

import android.content.Context;
import android.location.Location;

import org.fruct.oss.smarttrip.EventReceiver;
import org.fruct.oss.smarttrip.events.LocationEvent;
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
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;

import de.greenrobot.event.EventBus;

public class LocationLayer extends Layer {
	private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;

	private final Context context;

	private final Paint circleFill;
	private final Paint circleStroke;

	private final FixedPixelCircle circle;

	public LocationLayer(Context context) {
		this.context = context;

		circleFill = Utils.createPaint(GRAPHIC_FACTORY.createColor(127, 255, 0, 255), 0, Style.FILL);
		circleStroke = Utils.createPaint(GRAPHIC_FACTORY.createColor(127, 255, 125, 255), 2, Style.STROKE);

		circle = new FixedPixelCircle(null, Utils.getDP(5), circleFill, circleStroke);
	}

	@Override
	public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
	}

	@Override
	protected void onAdd() {
		super.onAdd();

		EventBus.getDefault().register(this);
		circle.setDisplayModel(displayModel);
	}

	@Override
	protected void onRemove() {
		EventBus.getDefault().unregister(this);

		super.onRemove();
	}

	@EventReceiver
	public void onEventMainThread(LocationEvent locationEvent) {
		Location location = locationEvent.getLocation();
		circle.setLatLong(new LatLong(location.getLatitude(), location.getLongitude()));

		requestRedraw();
	}
}
