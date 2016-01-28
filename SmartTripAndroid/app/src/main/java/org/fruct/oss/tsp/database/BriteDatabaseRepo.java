package org.fruct.oss.tsp.database;

import android.content.ContentValues;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.QueryObservable;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.database.tables.CurrentScheduleTable;
import org.fruct.oss.tsp.database.tables.PointsTable;
import org.fruct.oss.tsp.database.tables.ScheduleTable;

import java.util.Arrays;
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
	public Observable<List<Point>> loadCurrentSchedulePoints() {
		return db.createQuery(Arrays.asList(PointsTable.TABLE, CurrentScheduleTable.TABLE),
				"SELECT points._id, points.remoteId, points.title, points.lat, points.lon " +
						"FROM points JOIN currentSchedule ON points.scheduleId = currentSchedule.scheduleId;")
				.mapToList(PointsTable.MAPPER);
	}

	@Override
	public Observable<List<Point>> loadSchedulePoints(long scheduleId) {
		return db.createQuery(PointsTable.TABLE,
				"SELECT * from points WHERE points.scheduleId=?;", String.valueOf(scheduleId))
				.mapToList(PointsTable.MAPPER);
	}

	@Override
	public Observable<Schedule> loadCurrentSchedule() {
		return db.createQuery(Arrays.asList(ScheduleTable.TABLE, CurrentScheduleTable.TABLE),
				"SELECT schedule._id, schedule.title, schedule.tsptype, schedule.roadtype, schedule.startdate, schedule.enddate " +
						"FROM schedule JOIN currentSchedule ON schedule._id = currentSchedule.scheduleId;"
				)
				.mapToOne(ScheduleTable.MAPPER)
				.distinctUntilChanged();
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

	@Override
	public void setCurrentSchedule(long scheduleId) {
		ContentValues cv = new ContentValues(1);
		cv.put(CurrentScheduleTable.COLUMN_SCHEDULE_ID, scheduleId);
		db.update(CurrentScheduleTable.TABLE, cv, null);
	}

	@Override
	public void updateSchedule(long scheduleId, Schedule newSchedule) {
		ContentValues cv = ScheduleTable.toContentValues(newSchedule);
		db.update(ScheduleTable.TABLE, cv, "_id = ?", String.valueOf(scheduleId));
	}

	@Override
	public void deleteSchedule(long scheduleId) {
		BriteDatabase.Transaction transaction = db.newTransaction();
		try {
			ContentValues cv = new ContentValues(1);
			cv.putNull(CurrentScheduleTable.COLUMN_SCHEDULE_ID);
			db.update(CurrentScheduleTable.TABLE, cv, "scheduleId = ?", String.valueOf(scheduleId));
			db.delete(ScheduleTable.TABLE, "_id = ?", String.valueOf(scheduleId));
			transaction.markSuccessful();
		} finally {
			transaction.end();
		}
	}
}
