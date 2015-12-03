package org.fruct.oss.tsp;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {
	private static final Logger log = LoggerFactory.getLogger(App.class);
	@Override
	public void onCreate() {
		super.onCreate();
		log.info("App started");
	}
}
