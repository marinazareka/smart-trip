package org.fruct.oss.tsp;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.preference.PreferenceManager;

import org.fruct.oss.tsp.database.DatabaseOpenHelper;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class App extends Application {
	private static final Logger log = LoggerFactory.getLogger(App.class);

	private static App instance;

	private DatabaseOpenHelper databaseOpenHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		log.info("App started");
		PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, true);
		AndroidGraphicFactory.createInstance(this);

		setupStetho();
		setupDatabase();
	}

	@SuppressWarnings("TryWithIdenticalCatches")
	private void setupStetho() {
		if (BuildConfig.DEBUG) {
			try {
				Class<?> stethoClass = Class.forName("com.facebook.stetho.Stetho");
				Method initializeWithDefaults = stethoClass.getDeclaredMethod("initializeWithDefaults", Context.class);
				initializeWithDefaults.invoke(null, this);
			} catch (ClassNotFoundException e) {
				log.error("Can't init stetho", e);
			} catch (NoSuchMethodException e) {
				log.error("Can't init stetho", e);
			} catch (InvocationTargetException e) {
				log.error("Can't init stetho", e);
			} catch (IllegalAccessException e) {
				log.error("Can't init stetho", e);
			}
		}
	}
	public static Context getContext() {
		return instance.getApplicationContext();
	}

	public static App getInstance() {
		return instance;
	}

	private void setupDatabase() {
		databaseOpenHelper = new DatabaseOpenHelper(getApplicationContext());
		databaseOpenHelper.getWritableDatabase();
	}
}
