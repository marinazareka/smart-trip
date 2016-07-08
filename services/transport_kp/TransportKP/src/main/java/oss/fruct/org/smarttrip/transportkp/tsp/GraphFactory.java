package oss.fruct.org.smarttrip.transportkp.tsp;

import oss.fruct.org.smarttrip.transportkp.data.Point;

public interface GraphFactory {
	Graph createGraph(Point[] point);
}
