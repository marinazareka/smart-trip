package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;

import java.util.List;

public interface SchedulesMvpView {
	void setScheduleList(List<Schedule> scheduleList);
	void setSelectedScheduleId(long id);

	void displayEditDialog(Schedule schedule);
}
