package oss.fruct.org.smarttrip.transportkp.smartspace;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.data.RouteRequest;
import oss.fruct.org.smarttrip.transportkp.data.RouteResponse;

public class JnaSmartSpace implements SmartSpace {
	interface NativeLib extends Library {
		boolean init(String name, String smartspace, String address, int port);
		void shutdown();

		boolean subscribe();
		void unsubscribe();

		boolean wait_subscription(IntByReference out_points_count, PointerByReference out_points_pairs, PointerByReference data);
		void publish(int points_count, double[] points_pairs, Pointer data);

		// From libc
		void free(Pointer pointer);
	}

	private NativeLib lib;

	public JnaSmartSpace() {
		lib = (NativeLib) Native.loadLibrary("transport_kp", NativeLib.class);
	}

	@Override
	public boolean init(String name, String smartspace, String address, int port) {
		return lib.init(name, smartspace, address, port);
	}

	@Override
	public void shutdown() {
		lib.shutdown();
	}

	@Override
	public boolean subscribe() {
		return lib.subscribe();
	}

	@Override
	public void unsubscribe() {
		lib.unsubscribe();
	}

	@Override
	public RouteRequest waitSubscription() {
		PointerByReference array = new PointerByReference();
		IntByReference count = new IntByReference();
		PointerByReference data = new PointerByReference();

		if (!lib.wait_subscription(count, array, data)) {
			return null;
		}

		double[] pointPairs = array.getValue().getDoubleArray(0, count.getValue() * 2);

		Point[] points = new Point[count.getValue()];
		for (int i = 0; i < count.getValue(); i++) {
			points[i] = new Point(pointPairs[i * 2], pointPairs[i * 2 + 1]);
		}

		lib.free(array.getPointer());

		return new RouteRequest(data.getValue(), points);
	}

	@Override
	public void publish(RouteResponse response) {
		double[] pointPairs = new double[response.getRoute().length * 2];

		int c = 0;
		for (Point point : response.getRoute()) {
			pointPairs[c++] = point.getLat();
			pointPairs[c++] = point.getLon();
		}

		lib.publish(response.getRoute().length, pointPairs, (Pointer) response.getRequestTag());
	}
}