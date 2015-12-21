package oss.fruct.org.smarttrip.transportkp.annealing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.*;

/**
 * @param <S> type of state
 */
public class SimulatedAnnealing<S> {
	private static final Logger log = LoggerFactory.getLogger(SimulatedAnnealing.class);

	private ToDoubleFunction<S> energyFunction;
	private IntToDoubleFunction temperatureFunction;
	private Function<S, S> transitionFunction;

	private double tInit;
	private double tMin;
	private S stateInit;

	private int iter;
	private double temp;

	private S state;
	private double stateEnergy;

	private final Random random;

	public SimulatedAnnealing(Random random) {
		this.random = random;
	}

	public void setEnergyFunction(ToDoubleFunction<S> energyFunction) {
		this.energyFunction = energyFunction;
	}

	public void setTemperatureFunction(IntToDoubleFunction temperatureFunction) {
		this.temperatureFunction = temperatureFunction;
	}

	public void setTransitionFunction(Function<S, S> transitionFunction) {
		this.transitionFunction = transitionFunction;
	}

	public void setInitialState(double tInit, double tMin, S stateInit) {
		this.tInit = tInit;
		this.tMin = tMin;
		this.stateInit = stateInit;
	}

	public void start() {
		iter = 0;
		temp = tInit;
		state = stateInit;
		stateEnergy = energyFunction.applyAsDouble(state);
	}

	public boolean iter() {
		S stateCandidate = transitionFunction.apply(state);
		double stateCandidateEnergy = energyFunction.applyAsDouble(stateCandidate);

		double deltaE = stateCandidateEnergy - stateEnergy;
		log.trace("{}: temp {} delta {}", iter, temp, deltaE);

		if (deltaE < 0) {
			log.trace("Down");
			// Transition
			state = stateCandidate;
			stateEnergy = stateCandidateEnergy;
		} else {
			double transitionProbability = Math.exp(-deltaE / temp);
			log.trace(" Up prob {}...", transitionProbability);

			if (transitionProbability > random.nextDouble()) {
				log.trace("  OK");
				state = stateCandidate;
				stateEnergy = stateCandidateEnergy;
			} else {
				log.trace("  Fail");
			}
		}

		iter += 1;
		temp = temperatureFunction.applyAsDouble(iter);

		return temp > tMin;
	}


	public S getState() {
		return state;
	}

	public double getStateEnergy() {
		return stateEnergy;
	}
}
