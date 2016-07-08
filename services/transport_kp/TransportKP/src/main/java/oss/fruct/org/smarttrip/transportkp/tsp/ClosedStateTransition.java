package oss.fruct.org.smarttrip.transportkp.tsp;

import java.util.Arrays;
import java.util.Random;

public class ClosedStateTransition implements StateTransition {
	private Random random;

	public ClosedStateTransition(Random random) {
		this.random = random;
	}

	@Override
	public State transition(State state) {
		int[] path = Arrays.copyOf(state.getPath(), state.getPath().length);

		if (state.getPath().length < 4) {
			return state;
		}

		int i1, i2;
		do {
			i1 = random.nextInt(path.length - 2) + 1;
			i2 = random.nextInt(path.length - 2) + 1;
		} while (i1 == i2);

		reversePath(path, i1, i2);

		State newState = new State(path);

		return newState;
	}

	private void reversePath(int[] array, int i1, int i2) {
		if (i2 < i1) {
			reversePath(array, i2, i1);
		}

		while (i1 < i2) {
			int tmp = array[i1];
			array[i1] = array[i2];
			array[i2] = tmp;
			i1++;
			i2--;
		}
	}

}
