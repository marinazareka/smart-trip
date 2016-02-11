package oss.fruct.org.smarttrip.transportkp.smartspace;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.data.RouteRequest;
import oss.fruct.org.smarttrip.transportkp.data.RouteResponse;

import java.util.Arrays;
import java.util.List;

public class JnaSmartSpace implements SmartSpace {
	private static final Logger log = LoggerFactory.getLogger(JnaSmartSpace.class);

	private final String name;
	private final String smartspace;
	private final String address;
	private final int port;

	public static class RequestData extends Structure {
		public int count;
		public Pointer points;

		public String user_id;
		public double user_lat;
		public double user_lon;

		public String tsp_type;
		public String road_type;

		public Pointer point_individuals;
		public Pointer route;

		@Override
		protected List getFieldOrder() {
			return Arrays.asList("count", "points", "user_id", "user_lat", "user_lon", "tsp_type", "road_type", "point_individuals", "route");
		}
	}

	interface NativeLib extends Library {
		boolean init(String name, String smartspace, String address, int port);
		void shutdown();

		boolean subscribe();
		void unsubscribe();

		RequestData wait_subscription();
		void publish(int points_count, int[] ids, double[] weights, String roadType, RequestData requestData);

		// From libc
		void free(Pointer pointer);
	}

	private NativeLib lib;

	public JnaSmartSpace(String name, String smartspace, String address, int port) {
		this.name = name;
		this.smartspace = smartspace;
		this.address = address;
		this.port = port;
		lib = (NativeLib) Native.loadLibrary("transport_kp", NativeLib.class);
	}

	@Override
	public boolean init() {
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
		RequestData requestData;

		if ((requestData = lib.wait_subscription()) == null) {
			return null;
		}

		double[] pointPairs = requestData.points.getDoubleArray(0, 2 * requestData.count);

		Point[] points = new Point[requestData.count];
		for (int i = 0; i < requestData.count; i++) {
			points[i] = new Point(i, pointPairs[i * 2], pointPairs[i * 2 + 1]);
		}

		return new RouteRequest(requestData, requestData.user_id, points,
				new Point(-1, requestData.user_lat, requestData.user_lon), requestData.tsp_type, requestData.road_type);
	}

	@Override
	public void publish(RouteResponse response) {
		int[] ids = new int[response.getRoute().length];

		int c = 0;
		for (Point point : response.getRoute()) {
			ids[c++] = point.getId();
		}

		log.debug("Native publish {} {} {} {} {}", response.getRoute().length, ids, response.getWeights(),
				response.getRoadType(), (RequestData) response.getRequestTag());

		lib.publish(response.getRoute().length, ids, response.getWeights(),
				response.getRoadType(), (RequestData) response.getRequestTag());
	}
}
