package org.fruct.oss.smarttrip.location;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GoogleLocationUpdater
		implements LocationUpdater, com.google.android.gms.location.LocationListener {
	private GoogleApiClient client;
	private LocationListener listener;

	public GoogleLocationUpdater(GoogleApiClient client) {
		this.client = client;
	}

	@Override
	public void setListener(LocationListener locationListener) {
		this.listener = locationListener;
	}

	@Override
	public void start() {
		if (client.isConnected()) {
			LocationRequest locationRequest = new LocationRequest();
			locationRequest.setInterval(1000);
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

			LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
		}
	}

	@Override
	public void stop() {
		if (client.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (listener != null) {
			listener.onNewLocation(location);
		}
	}
}
