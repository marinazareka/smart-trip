package oss.fruct.org.smarttrip.transportkp;

import java.util.Objects;
import java.util.Random;

public class Utils {
	public static void shuffle(int[] array, int offset, int length, Random random) {
		int end = offset + length - 1;
		for (int i = end; i >= length + 1; i--) {
			int j = random.nextInt(i);

			int tmp = array[i];
			array[i] = array[j];
			array[j] = tmp;
		}
	}
}
