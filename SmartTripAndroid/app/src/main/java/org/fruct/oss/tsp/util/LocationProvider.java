package org.fruct.oss.tsp.util;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class LocationProvider {
	public static final LocationRequest REQUEST_MAP = LocationRequest.create()
			.setInterval(2000)
			.setFastestInterval(1000);

	public static final LocationRequest REQUEST_SMARTSPACE = LocationRequest.create()
			.setInterval(10000)
			.setFastestInterval(10000);

	public static Observable<Location> getObservable(Context context, LocationRequest request) {
		return Observable.create(new LocationOnSubscribe(context.getApplicationContext(), request));
	}

	private static class LocationOnSubscribe implements
			Observable.OnSubscribe<Location>, GoogleApiClient.ConnectionCallbacks,
			GoogleApiClient.OnConnectionFailedListener, LocationListener {
		private final Context context;
		private final LocationRequest request;

		private GoogleApiClient apiClient;
		private Subscriber<? super Location> subscriber;

		public LocationOnSubscribe(Context context, LocationRequest request) {
			this.context = context;
			this.request = request;
		}

		@Override
		public void call(Subscriber<? super Location> subscriber) {
			this.subscriber = subscriber;

			subscriber.add(Subscriptions.create(new Action0() {
				@Override
				public void call() {
					if (apiClient.isConnected() || apiClient.isConnecting()) {
						apiClient.disconnect();
					}
				}
			}));

			apiClient = new GoogleApiClient.Builder(context)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
			apiClient.connect();
		}

		@Override
		public void onConnected(Bundle bundle) {
			LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);

			Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
			subscriber.onNext(lastLocation);
		}

		@Override
		public void onConnectionSuspended(int i) {

		}

		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			subscriber.onError(new RuntimeException(connectionResult.getErrorMessage()));
		}

		@Override
		public void onLocationChanged(Location location) {
			subscriber.onNext(location);
		}
	}
}
