package org.fruct.oss.tsp.database;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;

import java.util.List;

import rx.Observable;

public interface DatabaseRepo {
	Observable<List<Schedule>> loadSchedules();
	long insertSchedule(Schedule schedule);
	void insertPoint(long scheduleId, Point point);
}
