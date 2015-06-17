package org.fruct.oss.smarttrip;

import android.app.Application;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		AndroidGraphicFactory.createInstance(this);
	}
}
