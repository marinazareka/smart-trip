package oss.fruct.org.smarttrip.transportkp;

import oss.fruct.org.smarttrip.transportkp.annealing.SimulatedAnnealing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OldTest {
	private static Random random = new Random();

	private static class Point {
		double x, y;

		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double distanceTo(Point p2) {
			double dx = p2.x - x;
			double dy = p2.y - y;
			return Math.sqrt(dx * dx + dy * dy);
		}
	}

	private static class Graph {
		int vertexCount;
		double[][] distanceMatrix;

		public Graph(Point[] points) {
			this(points.length);

			for (int i = 0; i < points.length; i++) {
				Point p1 = points[i];
				for (int j = 0; j < points.length; j++) {
					Point p2 = points[j];
					setDistance(i, j, p1.distanceTo(p2));
				}
			}
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

	private static class GraphState {
		private int[] path;
		private Graph graph;

		public GraphState(Graph graph, int vStart, int vEnd) {
			this.graph = graph;

			int size = graph.getVertexCount();
			path = new int[size];

			List<Integer> intSet = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				intSet.add(i);
			}

			intSet.remove(Integer.valueOf(vStart));
			path[0] = vStart;

			intSet.remove(Integer.valueOf(vEnd));
			path[size - 1] = vEnd;

			for (int i = 1; i < size - 1; i++) {
				path[i] = intSet.remove(random.nextInt(intSet.size()));
			}
		}

		public GraphState(GraphState graphState) {
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

		public GraphState transition() {
			GraphState newState = new GraphState(this);
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

		@Override
		public String toString() {
			return "GraphState{" +
					"path=" + Arrays.toString(path) +
					'}';
		}
	}

	private static void test2() {
		Point[] points = new Point[] {
				new Point(4, 1),

				new Point(1, 1),
				new Point(2, 3),
				new Point(1, 8),
				new Point(3, 11),
				new Point(5, 8),
				new Point(8, 10),
				new Point(7, 5),
				new Point(10, 7),

				new Point(11, 1),
		};

//		Point[] points = new Point[100];
//		for (int i = 0; i < points.length; i++) {
//			points[i] = new Point(i, i);
//		}

		Graph graph = new Graph(points);
		GraphState graphState = new GraphState(graph, 0, points.length - 1);

		double initialTemp = 1000;

		SimulatedAnnealing<GraphState> annealing = new SimulatedAnnealing<>(random);
		annealing.setEnergyFunction(state -> state.energy());
		annealing.setTransitionFunction(state -> state.transition());
		//annealing.setTemperatureFunction(i -> initialTemp * Math.pow(0.95, i));
		annealing.setTemperatureFunction(i -> initialTemp / i);
		//annealing.setInitialState(initialTemp, 0.5, graphState);

		annealing.start();

		while (annealing.iter()) {
			Main.log(annealing.getStateEnergy());
			Main.log("  ");
			Main.logln(annealing.getState());
		}

		System.err.println(annealing.getStateEnergy());
	}
}
