package org.fruct.oss.tsp.database;

import org.fruct.oss.tsp.commondatatype.Schedule;

import java.util.List;

public interface DatabaseRepo {
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	List<Schedule> loadSchedules();
}
