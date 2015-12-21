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

		if (isClosed) {
			pointsWithStart = new Point[points.length + 1];
			pointsWithStart[0] = startPoint;

			System.arraycopy(points, 0, pointsWithStart, 1, points.length);

			graph = graphFactory.createGraph(pointsWithStart);

			initialState = createStateClosed(graph);
		} else {
			throw new UnsupportedOperationException("Not implemented");
		}

		log.debug("Graph generation took " + (System.currentTimeMillis() - startTime));

		double initialTemp = 1000;

		SimulatedAnnealing<State> annealing = new SimulatedAnnealing<>(random);
		annealing.setEnergyFunction(state -> state.energy(graph));
		annealing.setTransitionFunction(state -> transition.transition(state));
		annealing.setTemperatureFunction(i -> initialTemp / i);
		annealing.setInitialState(initialTemp, 0.5, initialState);

		log.info("Starting annealing");
		annealing.start();

		while (annealing.iter()) {
			log.trace("Energy={}", annealing.getStateEnergy());
		}

		log.info("State energy {}", annealing.getStateEnergy());
		log.info("State path {}", annealing.getState());

		return new Result(annealing.getState().toPoints(pointsWithStart),
				annealing.getState().getPath(),
				annealing.getStateEnergy());
	}

	public static class Result {
		private Result(Point[] points, int[] path, double value) {
			this.points = points;
			this.path = path;
			this.value = value;
		}

		public Point[] points;
		public int[] path;
		public double value;
	}

	private State createStateClosed(Graph graph) {
		int size = graph.getVertexCount();

		// Last point in state equals first
		int[] path = new int[size + 1];

		for (int i = 1; i < size; i++) {
			path[i] = i;
		}

		Utils.shuffle(path, 1, size - 1, random);

		return new State(path);
	}
}
