package oss.fruct.org.smarttrip.transportkp.tsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.Utils;
import oss.fruct.org.smarttrip.transportkp.annealing.SimulatedAnnealing;
import oss.fruct.org.smarttrip.transportkp.data.Point;

import java.util.Random;

public class TravellingSalesman {
	private static final Logger log = LoggerFactory.getLogger(TravellingSalesman.class);

	private GraphFactory graphFactory;
	private StateTransition transition;

	private Point[] points;
	private Random random;

	public TravellingSalesman(GraphFactory graphFactory, StateTransition transition, Point[] points, Random random) {
		this.graphFactory = graphFactory;
		this.transition = transition;
		this.points = points;
		this.random = random;
	}

	public Result findPath(Point startPoint, boolean isClosed) {
		long startTime = System.currentTimeMillis();

		Point[] pointsWithStart;
		Graph graph;
		State initialState;

		pointsWithStart = new Point[points.length + 1];
		pointsWithStart[0] = startPoint;
		System.arraycopy(points, 0, pointsWithStart, 1, points.length);
		graph = graphFactory.createGraph(pointsWithStart);

		if (isClosed) {
			//initialState = createStateClosed(graph);
			initialState = createStateClosedNeighbour(graph);
		} else {
			initialState = createStateOpen(graph);
		}

		log.debug("Graph generation took " + (System.currentTimeMillis() - startTime));

		double initialTemp = 100;
		int iters = 1000;

		SimulatedAnnealing<State> annealing = new SimulatedAnnealing<>(random);
		annealing.setEnergyFunction(state -> state.energy(graph));
		annealing.setTransitionFunction(state -> transition.transition(state));
		annealing.setTemperatureFunction(v -> initialTemp / ((v * iters) + 1));
		annealing.setInitialState(iters, initialState);

		log.info("Starting annealing");
		annealing.start();

		while (annealing.iter()) {
			log.trace("Energy={}", annealing.getStateEnergy());
		}

		log.info("State energy {}", annealing.getStateEnergy());
		log.info("State path {}", annealing.getState());

		return new Result(annealing.getState().toPoints(pointsWithStart),
				annealing.getState().toWeights(graph),
				annealing.getState().getPath(),
				annealing.getStateEnergy());
	}

	public static class Result {
		Result(Point[] points, double[] weights, int[] path, double value) {
			this.points = points;
			this.weights = weights;
			this.path = path;
			this.value = value;
		}

		public Point[] points;
		public double[] weights;
		public int[] path;
		public double value;

		public boolean isEmpty() {
			return points.length == 0;
		}
	}

	private State createStateOpen(Graph graph) {
		int size = graph.getVertexCount();

		int[] path = new int[size];

		for (int i = 1; i < size; i++) {
			path[i] = i;
		}

		Utils.shuffle(path, 1, size, random);

		return new State(path);
	}

	private State createStateClosed(Graph graph) {
		int size = graph.getVertexCount();

		// Last point in state equals first
		int[] path = new int[size + 1];

		for (int i = 1; i < size; i++) {
			path[i] = i;
		}

		Utils.shuffle(path, 1, size, random);

		return new State(path);
	}

	private State createStateClosedNeighbour(Graph graph) {
		int size = graph.getVertexCount();

		int[] path = new int[size + 1];

		for (int i = 1; i < size; i++) {
			path[i] = i;
		}

		for (int i = 0; i < size; i++) {
			double minDistance = -1;
			int minNodeIndex = -1;
			for (int j = i + 1; j < size; j++) {
				double distance = graph.getWeight(path[i], path[j]);
				if (minDistance < 0 || distance < minDistance) {
					minDistance = distance;
					minNodeIndex = j;
				}
			}

			if (minDistance >= 0) {
				// Индексы могут быть равны
				int tmp = path[i + 1];
				path[i + 1] = path[minNodeIndex];
				path[minNodeIndex] = tmp;
			}
		}

		return new State(path);
	}
}
