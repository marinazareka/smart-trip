package oss.fruct.org.smarttrip.transportkp.tsp;

import oss.fruct.org.smarttrip.transportkp.data.Point;

public class EuclideanGraphFactory implements GraphFactory {
	@Override
	public Graph createGraph(Point[] points) {
		return generate(points);
	}

	public static Graph generate(Point[] points) {
		Graph graph = new Graph(points.length);

		for (int i = 0; i < points.length; i++) {
			Point p1 = points[i];
			for (int j = 0; j < points.length; j++) {
				Point p2 = points[j];
				if (p1 == p2) {
					graph.setWeight(i, j, 0);
				} else {
					graph.setWeight(i, j, distanceBetween(p1, p2));
				}
			}
		}

		return graph;
	}

	private static double distanceBetween(Point p1, Point p2) {
		double dx = p1.getLat() - p2.getLat();
		double dy = p1.getLon() - p2.getLon();

		return Math.hypot(dx, dy);
	}

}
