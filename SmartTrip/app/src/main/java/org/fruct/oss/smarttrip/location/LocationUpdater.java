package org.fruct.oss.smarttrip.location;

/**
 * Abstract location receiver
 */
public interface LocationUpdater {
	void setListener(LocationListener locationListener);
	void start();
	void stop();
}
