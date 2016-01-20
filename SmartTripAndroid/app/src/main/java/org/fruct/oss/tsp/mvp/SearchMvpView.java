package org.fruct.oss.tsp.mvp;

import android.support.annotation.Nullable;

import org.fruct.oss.tsp.commondatatype.Point;

import java.util.List;

public interface SearchMvpView {
	void setEmptyMode(boolean isEmptyModeEnabled);
	void setPointList(List<Point> pointList);

	void displaySearchDialog(@Nullable String initialPattern, int initialRadius);
	void displaySearchWaiter();
	void dismissSearchWaiter();
}
