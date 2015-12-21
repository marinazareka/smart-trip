package oss.fruct.org.smarttrip.transportkp.tsp;

import oss.fruct.org.smarttrip.transportkp.data.Point;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class State {
	private final Graph graph;
	private final int[] path;
	private Random random;

	public State(Graph graph, int[] path, Random random) {
		this.graph = graph;
		this.path = path;
		this.random = random;
	}

	public State(State graphState, Random random) {
		this.random = random;
		graph = graphState.graph;
		path = Arrays.copyOf(graphState.path, graphState.path.length);
	}

	public double energy() {
		double sum = 0;
		for (int i = 1; i < path.length; i++) {
			sum += graph.getDistance(path[i - 1], path[i]);
		}
		return sum;
	}

	public State transition() {
		State newState = new State(this, random);
		int i1, i2;
		do {
			i1 = random.nextInt(path.length - 2) + 1;
			i2 = random.nextInt(path.length - 2) + 1;
		} while (i1 == i2);

		newState.reversePath(i1, i2);

		return newState;
	}

	public void reversePath(int i1, int i2) {
		if (i2 < i1) {
			reversePath(i2, i1);
		}

		while (i1 < i2) {
			int tmp = path[i1];
			path[i1] = path[i2];
			path[i2] = tmp;
			i1++;
			i2--;
		}
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
}
