package org.fruct.oss.tsp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.fruct.oss.tsp.activities.MainActivity;
import org.fruct.oss.tsp.events.LocationEvent;

import de.greenrobot.event.EventBus;

public class LocationTrackingService extends Service
		implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, LocationListener {
	private static final String TAG = "LocationTrackingService";

	public static final String ACTION_START_TRACKING = "org.fruct.oss.tsp.LocationTrackingService.START";
	public static final String ACTION_STOP_TRACKING = "org.fruct.oss.tsp.LocationTrackingService.STOP";
	private GoogleApiClient apiClient;

	public LocationTrackingService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || intent.getAction() == null) {
			return START_NOT_STICKY;
		}

		switch (intent.getAction()) {
		case ACTION_START_TRACKING:
			startTracking();
			break;

		case ACTION_STOP_TRACKING:
			stopTracking();
			break;

		default:
			return super.onStartCommand(intent, flags, startId);
		}

		return START_STICKY;
	}

	private void stopTracking() {
		LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
		apiClient.disconnect();
	}

	private void startTracking() {
		apiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		apiClient.connect();
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onConnected(Bundle bundle) {
		LocationRequest request = LocationRequest.create()
				.setInterval(2000)
				.setFastestInterval(1000);

		LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);

		Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
		onLocationChanged(lastLocation);
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location received " + location);
		EventBus.getDefault().post(new LocationEvent(location));
	}

	public static void actionStartTracking(Context context) {
		context.startService(new Intent(ACTION_START_TRACKING, null, context, LocationTrackingService.class));
	}

	public static void actionStopTracking(Context context) {
		context.startService(new Intent(ACTION_STOP_TRACKING, null, context, LocationTrackingService.class));
	}
}
