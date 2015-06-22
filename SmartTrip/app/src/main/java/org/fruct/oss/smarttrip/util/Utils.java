package org.fruct.oss.smarttrip.util;

import android.content.Context;

import org.fruct.oss.smarttrip.App;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

public class Utils {
	public static int getDP(float dp) {
		Context context = App.getContext();

		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(dp * scale + 0.5f);
	}

	public static int getSP(float sp) {
		Context context = App.getContext();

		final float scale = context.getResources().getDisplayMetrics().scaledDensity;
		return  (int) (sp * scale + 0.5f);
	}

	public static Paint createPaint(int color, int width, Style style) {
		Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
		paint.setColor(color);
		paint.setStrokeWidth(width);
		paint.setStyle(style);
		return paint;
	}
}
