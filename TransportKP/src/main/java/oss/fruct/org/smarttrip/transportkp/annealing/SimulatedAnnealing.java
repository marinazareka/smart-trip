package oss.fruct.org.smarttrip.transportkp.annealing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.*;

import static oss.fruct.org.smarttrip.transportkp.Main.log;
import static oss.fruct.org.smarttrip.transportkp.Main.logln;

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
	private long seedInit;

	private int iter;
	private double temp;

	private S state;
	private double stateEnergy;

	private Random random;

	public void setEnergyFunction(ToDoubleFunction<S> energyFunction) {
		this.energyFunction = energyFunction;
	}

	public void setTemperatureFunction(IntToDoubleFunction temperatureFunction) {
		this.temperatureFunction = temperatureFunction;
	}

	public void setTransitionFunction(Function<S, S> transitionFunction) {
		this.transitionFunction = transitionFunction;
	}

	public void setInitialState(double tInit, double tMin, S stateInit, long seed) {
		this.tInit = tInit;
		this.tMin = tMin;
		this.stateInit = stateInit;
		this.seedInit = seed;
	}

	public void start() {
		iter = 0;
		temp = tInit;
		state = stateInit;
		stateEnergy = energyFunction.applyAsDouble(state);
		random = new Random(seedInit);
	}

	public boolean iter() {
		S stateCandidate = transitionFunction.apply(state);
		double stateCandidateEnergy = energyFunction.applyAsDouble(stateCandidate);

		double deltaE = stateCandidateEnergy - stateEnergy;
		log(iter + ": temp " + temp + " delta " + deltaE);
		if (deltaE < 0) {
			logln(" Down ");
			// Transition
			state = stateCandidate;
			stateEnergy = stateCandidateEnergy;
		} else {
			double transitionProbability = Math.exp(-deltaE / temp);
			log(" Up prob " + transitionProbability + " ... ");

			if (transitionProbability > random.nextDouble()) {
				log(" OK");
				state = stateCandidate;
				stateEnergy = stateCandidateEnergy;
			} else {
				log(" Fail");
			}
			logln("");
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
