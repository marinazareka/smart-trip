package oss.fruct.org.smarttrip.transportkp.tsp;

import oss.fruct.org.smarttrip.transportkp.data.Point;

import java.util.Arrays;
import java.util.stream.IntStream;

public class State {
	private final int[] path;

	public State(int[] path) {
		this.path = path;
	}

	public double energy(Graph graph) {
		double sum = 0;
		for (int i = 1; i < path.length; i++) {
			sum += graph.getWeight(path[i - 1], path[i]);
		}
		return sum;
	}

	public Point[] toPoints(Point[] points) {
		return IntStream.of(path)
				.mapToObj(i -> points[i])
				.toArray(size -> new Point[size]);
	}

	@Override
	public String toString() {
		return "State{" +
				"path=" + Arrays.toString(path) +
				'}';
	}

	public int[] getPath() {
		return path;
	}

	public double[] toWeights(Graph graph) {
		double[] ret = new double[path.length - 1];

		for (int i = 0; i < path.length - 1; i++) {
			ret[i] = graph.getWeight(path[i], path[i + 1]);
		}

		return ret;
	}
}
