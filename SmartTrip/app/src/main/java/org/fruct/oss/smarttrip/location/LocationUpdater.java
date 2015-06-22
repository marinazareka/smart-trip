package org.fruct.oss.smarttrip.location;

import android.location.Location;

/**
 * Abstract location receiver
 */
public interface LocationUpdater {
	void setListener(LocationListener locationListener);
	Location getLastLocation();
	void start();
	void stop();
}
