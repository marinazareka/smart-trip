package org.fruct.oss.tsp.database;

import org.fruct.oss.tsp.commondatatype.Schedule;

import java.util.List;

import rx.Observable;

public interface DatabaseRepo {
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	Observable<List<Schedule>> loadSchedules();
}
