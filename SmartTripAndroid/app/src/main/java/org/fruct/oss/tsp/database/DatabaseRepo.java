package org.fruct.oss.tsp.database;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;

import java.util.List;

import rx.Observable;

public interface DatabaseRepo {
	Observable<List<Schedule>> loadSchedules();
	Observable<List<Point>> loadCurrentSchedulePoints();
	Observable<Schedule> loadCurrentSchedule();

	long insertSchedule(Schedule schedule);
	void insertPoint(long scheduleId, Point point);
	void setCurrentSchedule(long scheduleId);

	void updateSchedule(long scheduleId, Schedule newSchedule);

	void deleteSchedule(long scheduleId);
}
