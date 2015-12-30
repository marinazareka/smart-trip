package org.fruct.oss.tsp.smartslognative;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.SmartSpaceNative;

import java.io.IOException;

public class JniSmartSpaceNative implements SmartSpaceNative {
	public static void loadNativeLibrary() {
		System.loadLibrary("smartstripnative");
	}

	@Override
	public native void initialize(String userId, String kpName, String smartSpaceName, String address, int port) throws IOException;

	@Override
	public native void shutdown();

	@Override
	public native void updateUserLocation(double lat, double lon);

	@Override
	public native void postSearchRequest(double radius, String pattern);

	@Override
	public native void postScheduleRequest(Point[] points, String tspType);

	@Override
	public native void setListener(Listener listener);
}
