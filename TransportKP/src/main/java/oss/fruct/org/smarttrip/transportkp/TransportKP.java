package oss.fruct.org.smarttrip.transportkp;

import com.graphhopper.GraphHopper;
import com.graphhopper.util.DistanceCalcEarth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.data.RouteRequest;
import oss.fruct.org.smarttrip.transportkp.data.RouteResponse;
import oss.fruct.org.smarttrip.transportkp.data.RouteState;
import oss.fruct.org.smarttrip.transportkp.smartspace.SmartSpace;
import oss.fruct.org.smarttrip.transportkp.tsp.*;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransportKP {
	private static final double RECALCULATE_DISTANCE = 20; // meters

	private Logger log = LoggerFactory.getLogger(TransportKP.class);

	private DistanceCalcEarth distanceCalcEarth;

	private RequestCache requestCache;
	private SmartSpace smartSpace;
	private GraphHopper graphHopper;
	private Random random = new Random();

	private volatile boolean isNeedShutdown = false;

	public TransportKP(SmartSpace smartSpace, GraphHopper graphHopper) {
		this.smartSpace = smartSpace;
		this.graphHopper = graphHopper;
		requestCache = new RequestCache();
		distanceCalcEarth = new DistanceCalcEarth();
	}

	public void start() {
		if (!smartSpace.init()) {
			throw new RuntimeException("Can't connect to smartspace");
		}

		if (!smartSpace.subscribe()) {
			throw new RuntimeException("Can't subscribe to schedule request");
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.debug("Running shutdown hook");
				isNeedShutdown = true;
				smartSpace.unsubscribe();
				smartSpace.shutdown();
			}
		});

		while(!isNeedShutdown) {
			process();
		}
	}

	private boolean process() {
		log.info("Waiting subscription");
		RouteRequest request = smartSpace.waitSubscription();
		if (request == null) {
			return true;
		}

		log.info("Received request {} with {} points", request.getTag(), request.getPoints().length);

		TravellingSalesman.Result result = processRequest(request);
		if (result != null && !result.isEmpty()) {
			Point[] points = result.points;
			double[] weights = result.weights;

			for (Point point : points) {
				log.debug(point.getLat() + " " + point.getLon());
			}

			requestCache.insert(request.getUserId(), new RouteState(request));

			smartSpace.publish(new RouteResponse(points, weights, "foot", request.getTag()));
		}

		return true;
	}

	private TravellingSalesman.Result processRequest(RouteRequest request) {
		RouteState routeState = requestCache.find(request.getUserId());

		// No previous request
		if (routeState == null) {
			log.debug("Process request: not previous request");
			return forcedProcessRequest(request);
		}

		RouteRequest lastProcessedRequest = routeState.getRouteRequest();

		// Tsp type changed
		if (!lastProcessedRequest.getTspType().equals(request.getTspType())) {
			log.debug("Process request: Tsp type changed");
			return forcedProcessRequest(request);
		}

		// User too far from initial position
		// TODO: path should be tracked to optimize recalculation
		if (distanceCalcEarth.calcDist(request.getUserPoint().getLat(),
				request.getUserPoint().getLon(),
				lastProcessedRequest.getUserPoint().getLat(),
				lastProcessedRequest.getUserPoint().getLon()) > RECALCULATE_DISTANCE) {
			log.debug("Process request: User went too far");
			return forcedProcessRequest(request);
		}

		// Point set changed
		if (!Arrays.equals(request.getPoints(), lastProcessedRequest.getPoints())) {
			log.debug("Process request: Point set changed");
			return forcedProcessRequest(request);
		}

		return null;
	}

	private TravellingSalesman.Result forcedProcessRequest(RouteRequest request) {
		boolean isClosed = "closed".equals(request.getTspType());
		StateTransition stateTransition = isClosed
				? new ClosedStateTransition(random)
				: new OpenStateTransition(random);

		TravellingSalesman tsp = new TravellingSalesman(new GraphhopperGraphFactory(graphHopper),
				stateTransition, request.getPoints(), random);

		TravellingSalesman.Result result = tsp.findPath(request.getUserPoint(), isClosed);
		return result;
	}

}
