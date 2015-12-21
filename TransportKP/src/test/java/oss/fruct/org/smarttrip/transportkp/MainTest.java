package oss.fruct.org.smarttrip.transportkp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.tsp.EuclideanGraphFactory;
import oss.fruct.org.smarttrip.transportkp.tsp.TravellingSalesman;

import java.util.Random;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class MainTest {
	private Point[] POINTS1 = new Point[] {
			point(1, 5),
			point(3, 4),
			point(5, 5)
	};


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

	@Test
	public void testClosedTspStartEndPoints() {
		Point start = point(1, 1);

		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(), POINTS1, new Random());
		TravellingSalesman.Result result = salesman.findPath(start, true);

		int[] path = result.path;
		Point[] points = result.points;

		assertEquals(5, path.length);
		assertEquals(0, path[0]);
		assertEquals(0, path[4]);

		assertSame(start, points[0]);
		assertSame(start, points[4]);
	}

	@Test
	public void testClosedTsp() {
		Point start = point(1, 1);

		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(), POINTS1, new Random(3));
		TravellingSalesman.Result result = salesman.findPath(start, true);

		int[] path = result.path;
		assertThat(path, anyOf(
				equalTo(path(0, 1, 3, 2, 0)),
				equalTo(path(0, 3, 2, 1, 0))));
	}

	@Test(timeout = 1000)
	public void testClosedTspMinimal() {
		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(), points(2, 2), new Random(2));
		TravellingSalesman.Result result = salesman.findPath(point(1, 1), true);

		assertThat(result.path, is(path(0, 1, 0)));
	}

	private static Point point(double x, double y) {
		return new Point(0, x, y);
	}

	private static Point[] points(double... coords) {
		if (coords.length % 2 != 0) {
			throw new IllegalArgumentException("Even coords expected");
		}

		Point[] points = new Point[coords.length / 2];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point(i, coords[2 * i], coords[2 * i + 1]);
		}
		return points;
	}

	private static int[] path(int... arg) {
		return arg;
	}
}