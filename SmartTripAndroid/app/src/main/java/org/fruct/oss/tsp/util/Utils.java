package org.fruct.oss.tsp.util;

import android.content.Context;

import org.fruct.oss.tsp.App;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

import java.util.Random;

public class Utils {
	private static final Random random = new Random();

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

	public static String randomName() {
		StringBuilder sb = new StringBuilder();

		final String vow = "aeiouy";
		final String cons = "qwrtpsdfghjklzxcvbnm";
		final String[] endings = {
			"berg", "kva", "zero", "tie", "yarvi", "burg", "borg", "grad", "-city", "hell", "fall"
		};

		int length = random.nextInt(2) + 3;
		for (int i = 0; i < length; i++) {
			if (random.nextBoolean()) {
				sb.append(vow.charAt(random.nextInt(vow.length())));
				sb.append(cons.charAt(random.nextInt(cons.length())));
			} else {
				sb.append(cons.charAt(random.nextInt(cons.length())));
				sb.append(vow.charAt(random.nextInt(vow.length())));
			}
		}
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.append(endings[random.nextInt(endings.length)]);

		return sb.toString();
	}
}
