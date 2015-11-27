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

/**
 * Сервис отслеживания местоположения пользователя
 *
 * Уведомления об обновления местоположения пользователя происходят через событие {@link LocationEvent}
 * Для получения местоположения используются сервисы Google Play
 */
public class LocationTrackingService extends Service
		implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, LocationListener {
	private static final String TAG = "LocationTrackingService";

	private GoogleApiClient apiClient;
	private boolean isLocationUpdatesSubscribed;

	public LocationTrackingService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		apiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		apiClient.connect();
	}

	@Override
	public void onDestroy() {
		if (isLocationUpdatesSubscribed) {
			LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
		}
		apiClient.disconnect();
		apiClient = null;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_STICKY;
		}

		return START_STICKY;
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
		isLocationUpdatesSubscribed = true;
	}

	@Override
	public void onConnectionSuspended(int i) {
		isLocationUpdatesSubscribed = false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location received " + location);
		EventBus.getDefault().post(new LocationEvent(location));
	}

	/**
	 * Запуск сервиса
	 * @param context
	 */
	public static void actionStartTracking(Context context) {
		context.startService(new Intent(context, LocationTrackingService.class));
	}

	/**
	 * Остановка сервиса
	 * @param context
	 */
	public static void actionStopTracking(Context context) {
		context.stopService(new Intent(context, LocationTrackingService.class));
	}
}
