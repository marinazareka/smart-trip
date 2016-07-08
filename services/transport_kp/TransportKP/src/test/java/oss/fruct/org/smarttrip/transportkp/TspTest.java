package oss.fruct.org.smarttrip.transportkp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import oss.fruct.org.smarttrip.transportkp.data.Point;
import oss.fruct.org.smarttrip.transportkp.tsp.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TspTest {
	private Point[] POINTS1 = new Point[] {
			point(1, 5),
			point(3, 4),
			point(5, 5)
	};

	@Rule
	public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

	@Test
	public void testClosedTspStartEndPoints() {
		Point start = point(1, 1);
		Random random = new Random();

		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(),
				new ClosedStateTransition(random), POINTS1, random);
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
		Random random = new Random(3);


		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(),
				new ClosedStateTransition(random), POINTS1, random);
		TravellingSalesman.Result result = salesman.findPath(start, true);

		assertThat(result.path, anyOf(
				equalTo(path(0, 1, 3, 2, 0)),
				equalTo(path(0, 2, 3, 1, 0))));
	}

	@Test(timeout = 1000)
	public void testClosedTspMinimal() {
		Random random = new Random(1);

		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(),
				new ClosedStateTransition(random), points(2, 2), random);
		TravellingSalesman.Result result = salesman.findPath(point(1, 1), true);

		assertThat(result.path, is(path(0, 1, 0)));
	}

	@Test(timeout = 1000)
	public void testTransitionInfiniteLoop() {
		ClosedStateTransition transition = new ClosedStateTransition(new Random());
		transition.transition(new State(path(0, 1, 2)));
	}

	@Test
	public void testSquareClosed() {
			Random random = new Random(1);

			// generate square points

			List<Point> pointList = new ArrayList<>();

			int n = 3;
			for (int i = 0; i < n; i++) {
				pointList.add(point(i + 1, 0, i));
				pointList.add(point(i + 4, i, n));

				pointList.add(point(i + 7, n, n - i));
				pointList.add(point(i + 10, n - i, 0));
			}


			Point[] points = pointList.toArray(new Point[pointList.size()]);
			TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(),
					new ClosedStateTransition(random), points, random);
			TravellingSalesman.Result result = salesman.findPath(point(-1, 0), true);
			assertThat(Arrays.stream(result.points).mapToInt(Point::getId).toArray(), anyOf(
					equalTo(path(0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 0)),
					equalTo(path(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 0)),
					equalTo(path(0, 1, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 0)),
					equalTo(path(0, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0))));
	}

	@Test
	public void testOpenRandom() {
		for (int i = 0; i < 100; i++) {
			Random random = new Random(5 * i + 2234728882398L);
			Point[] points = new Point[10];
			for (int j = 0; j < 10; j++) {
				points[j] = point(random.nextDouble() * 100, random.nextDouble() * 100);
			}

			TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(),
					new OpenStateTransition(random), points, random);
			TravellingSalesman.Result result = salesman.findPath(point(0, 0), false);

			// Check unique
			Set<Integer> existing = new HashSet<>();
			for (int i1 : result.path) {
				assertFalse(existing.contains(i1));
				existing.add(i1);
			}

			assertThat(result.path[0], is(0));
		}
	}

	@Test
	public void testOpenLine() {
		Random random = new Random(1);

		Point[] points = new Point[10];
		for (int i = 0; i < 10; i++) {
			points[i] = point(i + 1, i, 0);
		}

		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(),
				new OpenStateTransition(random), points, random);
		TravellingSalesman.Result result = salesman.findPath(point(-1, 0), false);

		assertThat(result.path, is(path(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
	}

	@Test
	public void testOpenMinimal() {
		Random random = new Random(1);

		Point[] points = new Point[]{point(1, 1)};
		TravellingSalesman salesman = new TravellingSalesman(new EuclideanGraphFactory(),
				new OpenStateTransition(random), points, random);
		TravellingSalesman.Result result = salesman.findPath(point(0, 0), false);

		assertThat(result.path, is(path(0, 1)));
	}

	private static Point point(double x, double y) {
		return new Point(0, x, y);
	}

	private static Point point(int id, double x, double y) {
		return new Point(id, x, y);
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
