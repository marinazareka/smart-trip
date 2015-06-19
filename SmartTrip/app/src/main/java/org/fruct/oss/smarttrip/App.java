package org.fruct.oss.smarttrip;

import android.app.Application;
import android.content.Context;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

public class App extends Application {
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();

		AndroidGraphicFactory.createInstance(this);

		App.context = getApplicationContext();
	}

	public static Context getContext() {
		return context;
	}
}
