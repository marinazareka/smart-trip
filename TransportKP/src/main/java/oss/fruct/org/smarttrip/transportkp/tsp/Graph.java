package oss.fruct.org.smarttrip.transportkp.tsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Graph {
	private static final Logger log = LoggerFactory.getLogger(Graph.class);

	private double[][] distanceMatrix;
	private double[][] weightMatrix;
	private int vertexCount;

	public Graph(int vertexCount) {
		this.vertexCount = vertexCount;
		this.weightMatrix = new double[vertexCount][vertexCount];
		this.distanceMatrix = new double[vertexCount][vertexCount];
	}

	public void setWeight(int i, int j, double d) {
		weightMatrix[i][j] = d;
	}

	public double getWeight(int i, int j) {
		return weightMatrix[i][j];
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
