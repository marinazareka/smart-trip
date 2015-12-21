package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Point;

import java.util.List;

public interface PointListMvpView {
	void setPointList(List<Point> points);
	void setScheduleMenuActionVisible();

	void displaySearchDialog();

	void displaySearchWaiter();
	void dismissSearchWaiter();

	void displayFoundPoints(List<Point> points);
}

