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
	private DoubleUnaryOperator temperatureFunction;
	private Function<S, S> transitionFunction;

	private double tInit;
	private double tMin;
	private S stateInit;
	private int iterMax;

	private int iter;

	private S state;
	private double stateEnergy;

	private final Random random;

	public SimulatedAnnealing(Random random) {
		this.random = random;
	}

	public void setEnergyFunction(ToDoubleFunction<S> energyFunction) {
		this.energyFunction = energyFunction;
	}

	public void setTemperatureFunction(DoubleUnaryOperator temperatureFunction) {
		this.temperatureFunction = temperatureFunction;
	}

	public void setTransitionFunction(Function<S, S> transitionFunction) {
		this.transitionFunction = transitionFunction;
	}

	public void setInitialState(int iterMax, S stateInit) {
		this.stateInit = stateInit;
		this.iterMax = iterMax;
	}

	public void start() {
		iter = 0;
		state = stateInit;
		stateEnergy = energyFunction.applyAsDouble(state);
	}

	public boolean iter() {
		if (iterMax == 0)
			return false;

		S stateCandidate = transitionFunction.apply(state);
		double stateCandidateEnergy = energyFunction.applyAsDouble(stateCandidate);
		if (!Double.isNaN(stateCandidateEnergy)) {

			double temp = temperatureFunction.applyAsDouble((double) iter / iterMax);

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
		} else {
			log.trace("Invalid state");
		}

		iter += 1;
		return iter < iterMax;
	}


	public S getState() {
		return state;
	}

	public double getStateEnergy() {
		return stateEnergy;
	}
}
