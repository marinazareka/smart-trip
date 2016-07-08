package oss.fruct.org.smarttrip.transportkp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.tsp.ClosedStateTransition;
import oss.fruct.org.smarttrip.transportkp.tsp.EuclideanGraphFactory;
import oss.fruct.org.smarttrip.transportkp.tsp.State;
import oss.fruct.org.smarttrip.transportkp.tsp.TravellingSalesman;

import java.util.Random;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class MainTest {



	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void testGetRandomNumber() throws Exception {
		assertEquals(4, Main.getRandomNumber());
	}

	@Test
	public void testCanGoWrong() throws Exception {
		expectedException.expect(Exception.class);
		Main.canGoWrong();
	}


}