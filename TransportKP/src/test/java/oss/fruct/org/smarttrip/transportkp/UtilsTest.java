package oss.fruct.org.smarttrip.transportkp;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class UtilsTest {
	@Test
	public void testShuffle() {
		int[] array = new int[]{0, 1, 2, 3, 4, 5};
		Utils.shuffle(array, 1, 4, new Random());
		assertEquals(0, array[0]);
		assertEquals(5, array[5]);

	}
}