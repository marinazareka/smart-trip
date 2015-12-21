package org.fruct.oss.tsp;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.preference.PreferenceManager;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {
	private static final Logger log = LoggerFactory.getLogger(App.class);

	private static App instance;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		log.info("App started");
		PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, true);
		AndroidGraphicFactory.createInstance(this);
	}

	public static Context getContext() {
		return instance.getApplicationContext();
	}
}
