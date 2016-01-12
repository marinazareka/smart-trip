package org.fruct.oss.tsp.database;

import com.squareup.sqlbrite.BriteDatabase;

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
}
