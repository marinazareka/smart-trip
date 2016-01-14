package org.fruct.oss.tsp.smartslognative;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.SmartSpaceNative;

import java.io.IOException;

public class JniSmartSpaceNative implements SmartSpaceNative {
	public static void loadNativeLibrary() {
		System.loadLibrary("smartstripnative");
	}

	@Override
	public synchronized native void initialize(String userId, String kpName, String smartSpaceName, String address, int port) throws IOException;

	@Override
	public synchronized native void shutdown();

	@Override
	public synchronized native void updateUserLocation(double lat, double lon);

	@Override
	public synchronized native void postSearchRequest(double radius, String pattern);

	@Override
	public synchronized native void postScheduleRequest(Point[] points, String tspType);

	@Override
	public synchronized native void setListener(Listener listener);
}
