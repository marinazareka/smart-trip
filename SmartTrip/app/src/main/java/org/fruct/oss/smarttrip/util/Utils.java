package org.fruct.oss.smarttrip.util;

import android.content.Context;

import org.fruct.oss.smarttrip.App;

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

}
