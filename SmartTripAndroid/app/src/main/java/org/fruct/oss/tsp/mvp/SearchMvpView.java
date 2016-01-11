package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Point;

import java.util.List;

public interface SearchMvpView {
	void setEmptyMode(boolean isEmptyModeEnabled);
	void setPointList(List<Point> pointList);
}
