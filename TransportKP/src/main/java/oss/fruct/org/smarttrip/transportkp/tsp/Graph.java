package oss.fruct.org.smarttrip.transportkp.tsp;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.AStarBidirection;
import com.graphhopper.routing.DijkstraOneToMany;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.*;
import com.graphhopper.storage.index.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.data.Point;

import java.util.stream.Stream;

public class Graph {
	private static final Logger log = LoggerFactory.getLogger(Graph.class);

	private double[][] distanceMatrix;
	private int vertexCount;

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
