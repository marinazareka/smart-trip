package oss.fruct.org.smarttrip.transportkp.tsp;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.data.Point;

public class Graph {
	private static final Logger log = LoggerFactory.getLogger(Graph.class);

	private double[][] distanceMatrix;
	private int vertexCount;

	public static Graph generate(GraphHopper graphHopper, Point[] points) {
		Graph graph = new Graph(points.length);

		for (int i = 0; i < points.length; i++) {
			Point p1 = points[i];
			for (int j = 0; j < points.length; j++) {
				Point p2 = points[j];
				if (p1 == p2) {
					graph.setDistance(i, j, 0);
				} else {
					graph.setDistance(i, j, distanceBetween(graphHopper, p1, p2));
				}
			}
		}

		return graph;
	}

	private static double distanceBetween(GraphHopper graphHopper, Point p1, Point p2) {
		GHRequest request = new GHRequest(p1.getLat(), p1.getLon(), p2.getLat(), p2.getLon())
				.setWeighting("shortest")
				.setVehicle("foot");

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

	public Graph(int vertexCount) {
		this.vertexCount = vertexCount;
		this.distanceMatrix = new double[vertexCount][vertexCount];
	}

	public void setDistance(int i, int j, double d) {
		distanceMatrix[i][j] = d;
	}

	public double getDistance(int i, int j) {
		return distanceMatrix[i][j];
	}

	public int getVertexCount() {
		return vertexCount;
	}
}
