package org.fruct.oss.tsp.mvp;

import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.database.DatabaseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SchedulesPresenter implements Presenter<SchedulesMvpView> {
	private static final Logger log = LoggerFactory.getLogger(SchedulesPresenter.class);

	private DatabaseRepo databaseRepo;
	private SchedulesMvpView view;

	private Subscription subscription;

	public SchedulesPresenter(DatabaseRepo databaseRepo) {
		this.databaseRepo = databaseRepo;
	}

	@Override
	public void setView(SchedulesMvpView view) {
		this.view = view;
	}

	@Override
	public void start() {
		this.subscription = databaseRepo.loadSchedules()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<List<Schedule>>() {
					@Override
					public void call(List<Schedule> schedules) {
						view.setScheduleList(schedules);
					}
				});
	}

	@Override
	public void stop() {
		subscription.unsubscribe();
	}

	public void onScheduleClicked(Schedule schedule) {
		log.debug("{} schedule clicked", schedule.getTitle());
		view.setSelectedSchedule(schedule);
	}
}
