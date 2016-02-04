package oss.fruct.org.smarttrip.transportkp.tsp;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.DijkstraOneToMany;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.*;
import com.graphhopper.storage.index.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.data.Point;

import java.util.stream.Stream;

public class GraphhopperGraphFactory implements GraphFactory {
	private static final Logger log = LoggerFactory.getLogger(GraphhopperGraphFactory.class);

	private GraphHopper graphHopper;
	private RoadType roadType;

	public enum RoadType {
		CAR, FOOT, BUS
	}

	public GraphhopperGraphFactory(GraphHopper graphHopper, RoadType roadType) {
		this.graphHopper = graphHopper;
		this.roadType = roadType;
	}

	public static Graph generate(GraphHopper hopper, Point[] points, RoadType roadType) {
		Graph graph = new Graph(points.length);


		for (int i = 0; i < points.length; i++) {
			Point p1 = points[i];
			for (int j = 0; j < points.length; j++) {
				Point p2 = points[j];
				if (p1 == p2) {
					graph.setWeight(i, j, 0);
					graph.setDistance(i, j, 0);
				} else {
					double d = distanceBetween(hopper, p1, p2, roadType);
					graph.setWeight(i, j, d);
					graph.setDistance(i, j, d);
				}
			}
		}

		return graph;
	}

	public static Graph generateFast(GraphHopper hopper, Point[] points, RoadType roadType) {
		Graph graph = new Graph(points.length);
		int[] nodes = findNodes(hopper, points);

		log.debug(nodes.length + " nodes found");

		FlagEncoder encoder = hopper.getEncodingManager().getEncoder(roadType.name().toLowerCase());

		DijkstraOneToMany dijkstraOneToMany = new DijkstraOneToMany(
				hopper.getGraphHopperStorage(),
				encoder,
				new FastestWeighting(encoder),
				TraversalMode.NODE_BASED);

		for (int i = 0; i < nodes.length; i++) {
			int fromNode = nodes[i];
			dijkstraOneToMany.clear();

			for (int j = 0; j < nodes.length; j++) {
				int toNode = nodes[j];
				if (toNode == fromNode) {
					graph.setWeight(i, j, 0);
					graph.setDistance(i, j, 0);

				} else {
					Path path = dijkstraOneToMany.calcPath(fromNode, toNode);
					graph.setWeight(i, j, path.getWeight());
					graph.setDistance(i, j, path.getDistance());
				}
			}
		}

		return graph;
	}

	private static int[] findNodes(GraphHopper hopper, Point[] points) {
		FlagEncoder encoder = hopper.getEncodingManager().getEncoder("foot");
		EdgeFilter edgeFilter = new DefaultEdgeFilter(encoder);

		return Stream.of(points)
				.map(point -> hopper.getLocationIndex().findClosest(point.getLat(), point.getLon(), edgeFilter))
				.filter(QueryResult::isValid)
				.mapToInt(QueryResult::getClosestNode)
				.toArray();
	}

	private static double distanceBetween(GraphHopper graphHopper, Point p1, Point p2, RoadType roadType) {
		GHRequest request = new GHRequest(p1.getLat(), p1.getLon(), p2.getLat(), p2.getLon())
				.setWeighting("fastest")
				.setVehicle(roadType.name().toLowerCase());

		GHResponse response = graphHopper.route(request);
		if (response.hasErrors()) {
			log.debug("Can't find path from {} to {}:", p1, p2);
			for (Throwable throwable : response.getErrors()) {
				log.debug("Graphhopper error: ", throwable);
			}
			return Double.POSITIVE_INFINITY;
		}

		return response.getDistance();
	}

	@Override
	public Graph createGraph(Point[] points) {
		return generateFast(graphHopper, points, roadType);
	}
}
