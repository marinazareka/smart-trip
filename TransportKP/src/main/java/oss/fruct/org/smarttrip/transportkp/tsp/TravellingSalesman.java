package oss.fruct.org.smarttrip.transportkp.tsp;

import com.graphhopper.GraphHopper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oss.fruct.org.smarttrip.transportkp.annealing.SimulatedAnnealing;
import oss.fruct.org.smarttrip.transportkp.data.Point;

public class TravellingSalesman {
	private static final Logger log = LoggerFactory.getLogger(TravellingSalesman.class);

	private GraphHopper graphHopper;
	private Point[] points;

	public TravellingSalesman(GraphHopper graphHopper, Point[] points) {
		this.graphHopper = graphHopper;
		this.points = points;
	}

	public Point[] findPath() {
		Graph graph = Graph.generate(graphHopper, points);

		double initialTemp = 1000;
		State initialState = new State(graph, 0, points.length - 1);

		SimulatedAnnealing<State> annealing = new SimulatedAnnealing<>();
		annealing.setEnergyFunction(state -> state.energy());
		annealing.setTransitionFunction(state -> state.transition());
		annealing.setTemperatureFunction(i -> initialTemp / i);
		annealing.setInitialState(initialTemp, 0.5, initialState, System.currentTimeMillis());

		log.info("Starting annealing");
		annealing.start();

		while (annealing.iter()) {
			log.debug("Energy={}", annealing.getStateEnergy());
		}

		log.info("State energy {}", annealing.getStateEnergy());
		log.info("State path {}", annealing.getState());

		return annealing.getState().toPoints(points);
	}
}
