package org.fruct.oss.tsp.smartslognative;

import org.fruct.oss.tsp.commondatatype.Point;

public class JniSmartSpaceNative implements SmartSpaceNative {
	static {
		System.loadLibrary("smartstripnative");
	}

	@Override
	public native void initialize(String userId);

	@Override
	public native void shutdown();

	@Override
	public native void updateUserLocation(double lat, double lon);

	@Override
	public native void postSearchRequest(double radius, String pattern);

	@Override
	public native void postScheduleRequest(Point[] points) ;

	@Override
	public native void setListener(Listener listener);
}
