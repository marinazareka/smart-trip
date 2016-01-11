package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.database.DatabaseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulesPresenter implements Presenter<SchedulesMvpView> {
	private static final Logger log = LoggerFactory.getLogger(SchedulesPresenter.class);

	private DatabaseRepo databaseRepo;
	private SchedulesMvpView view;

	public SchedulesPresenter(DatabaseRepo databaseRepo) {
		this.databaseRepo = databaseRepo;
	}

	@Override
	public void setView(SchedulesMvpView view) {
		this.view = view;
	}

	@Override
	public void start() {
		view.setScheduleList(databaseRepo.loadSchedules());
	}

	@Override
	public void stop() {

	}

	public void onScheduleClicked(Schedule schedule) {
		log.debug("{} schedule clicked", schedule.getTitle());
	}
}
