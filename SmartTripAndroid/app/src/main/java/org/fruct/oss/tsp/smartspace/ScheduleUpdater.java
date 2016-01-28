package org.fruct.oss.tsp.smartspace;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.Schedule;
import org.fruct.oss.tsp.commondatatype.TspType;
import org.fruct.oss.tsp.database.DatabaseRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Вспомогательный класс для обновления Schedule-запроса в smartspace'е
 * Отслеживает следующее
 * <ul>
 *     <li>Местоположение пользователя</li>
 *     <li>Набор точек для текущего маршрута</li>
 *     <li>Тип текущего маршрута</li>
 * </ul>
 */
public class ScheduleUpdater {
	private static final Logger log = LoggerFactory.getLogger(ScheduleUpdater.class);

	private final Context context;
	private final SharedPreferences pref;
	//private final RxSharedPreferences rxPref;
	private final DatabaseRepo repo;
	private final SmartSpace smartSpace;

	private final Observable<Location> locationObservable;

	private Observable<List<Point>> currentSchedulePointsObservable;
	private Observable<Schedule> currentScheduleTypeObservable;

	private Subscription scheduleDataSubscription;
	private Subscription locationSubscription;

	public ScheduleUpdater(Context context, Observable<Location> locationObservable,
						   DatabaseRepo repo, SmartSpace smartSpace) {
		this.context = context;
		this.locationObservable = locationObservable;
		this.repo = repo;
		this.smartSpace = smartSpace;
		this.pref = PreferenceManager.getDefaultSharedPreferences(context);
		//this.rxPref = RxSharedPreferences.create(pref);
	}

	public void start() {
		currentSchedulePointsObservable = repo.loadCurrentSchedulePoints();
		currentScheduleTypeObservable = repo.loadCurrentSchedule()
				.distinctUntilChanged();

		scheduleDataSubscription = Observable.combineLatest(
				currentSchedulePointsObservable, currentScheduleTypeObservable,
				new Func2<List<Point>, Schedule, ScheduleCombined>() {
					@Override
					public ScheduleCombined call(List<Point> points, Schedule schedule) {
						return new ScheduleCombined(points, schedule);
					}
				})
				.debounce(5, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Action1<ScheduleCombined>() {
					@Override
					public void call(ScheduleCombined scheduleCombined) {
						publishNewScheduleData(scheduleCombined);
					}
				});

		locationSubscription = locationObservable
				.subscribeOn(Schedulers.io())
				.subscribe(new Action1<Location>() {
					@Override
					public void call(Location location) {
						smartSpace.updateUserLocation(location);
					}
				});
	}

	private void publishNewScheduleData(ScheduleCombined scheduleCombined) {
		log.debug("publishNewScheduleData " + scheduleCombined.toString());
		Schedule schedule = scheduleCombined.schedule;
		smartSpace.postScheduleRequest(scheduleCombined.points, schedule);
	}

	public void stop() {
		scheduleDataSubscription.unsubscribe();
		locationSubscription.unsubscribe();
	}

	class ScheduleCombined {
		private final List<Point> points;
		private final Schedule schedule;

		public ScheduleCombined(List<Point> points, Schedule schedule) {
			this.points = points;
			this.schedule = schedule;
		}
	}
}
