package org.fruct.oss.tsp.mvp;

import android.support.annotation.Nullable;

import org.fruct.oss.tsp.commondatatype.Point;

import java.util.List;

public interface PointListMvpView {
	void setPointList(List<Point> points);
	void setScheduleMenuActionVisibility(boolean b);

	void displaySearchDialog(@Nullable String initialPattern, int initialRadius);

	void displaySearchWaiter();
	void dismissSearchWaiter();

	void displayFoundPoints(List<Point> points);
}

