package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Schedule;

import java.util.List;

public interface SchedulesMvpView {
	void setScheduleList(List<Schedule> scheduleList);
	void setSelectedSchedule(Schedule schedule);
}
