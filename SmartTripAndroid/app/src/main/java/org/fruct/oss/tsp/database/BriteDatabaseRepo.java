package org.fruct.oss.tsp.database;

import com.squareup.sqlbrite.BriteDatabase;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;

import java.util.List;

import rx.Observable;

public class BriteDatabaseRepo implements DatabaseRepo {
	private final BriteDatabase db;

	public BriteDatabaseRepo(BriteDatabase db) {
		this.db = db;
	}

	@Override
	public Observable<List<Schedule>> loadSchedules() {
		return db.createQuery(ScheduleTable.TABLE, ScheduleTable.queryGet())
				.mapToList(ScheduleTable.MAPPER);
	}

	@Override
	public long insertSchedule(Schedule schedule) {
		if (schedule.getId() != 0) {
			throw new IllegalArgumentException("Schedule must not be in database");
		}

		return db.insert(ScheduleTable.TABLE, ScheduleTable.toContentValues(schedule));
	}

	@Override
	public void insertPoint(long scheduleId, Point point) {
		db.insert(PointsTable.TABLE, PointsTable.toContentValues(point, scheduleId));
	}
}
