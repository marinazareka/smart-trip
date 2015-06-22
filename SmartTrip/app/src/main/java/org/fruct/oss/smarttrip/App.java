package org.fruct.oss.smarttrip;

import android.app.Application;
import android.content.Context;

import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

public class App extends Application {
	private static App app;

	private JobManager jobManager;

	public App() {
		app = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		setupMapsforge();
		setupPriorityJobQueue();
	}

	private void setupPriorityJobQueue() {
		Configuration configuration = new Configuration.Builder(this)
				.maxConsumerCount(1)
				.minConsumerCount(0)
				.loadFactor(1)
				.consumerKeepAlive(120)
				.build();

		jobManager = new JobManager(this, configuration);
	}

	private void setupMapsforge() {
		AndroidGraphicFactory.createInstance(this);
	}

	public static Context getContext() {
		return app.getApplicationContext();
	}

	public static JobManager getJobManager() {
		return app.jobManager;
	}
}
