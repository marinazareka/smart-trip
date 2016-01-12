package org.fruct.oss.tsp.mvp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.database.DatabaseRepo;
import org.fruct.oss.tsp.util.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SchedulesPresenter implements Presenter<SchedulesMvpView> {
	private static final Logger log = LoggerFactory.getLogger(SchedulesPresenter.class);

	private final Context context;
	private final DatabaseRepo databaseRepo;
	private final SharedPreferences pref;

	private SchedulesMvpView view;
	private Subscription subscription;

	public SchedulesPresenter(Context context, DatabaseRepo databaseRepo) {
		this.context = context;
		this.databaseRepo = databaseRepo;
		this.pref = PreferenceManager.getDefaultSharedPreferences(context);
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

		updateCurrentSchedule();
	}

	@Override
	public void stop() {
		subscription.unsubscribe();
	}

	public void onScheduleClicked(Schedule schedule) {
		Pref.setCurrentSchedule(pref, schedule.getId());
		updateCurrentSchedule();
	}

	private void updateCurrentSchedule() {
		view.setSelectedScheduleId(Pref.getCurrentSchedule(pref));
	}
}
