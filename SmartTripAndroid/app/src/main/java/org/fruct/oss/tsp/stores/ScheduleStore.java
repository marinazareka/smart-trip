package org.fruct.oss.tsp.stores;

import android.util.Log;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.events.ScheduleEvent;
import org.fruct.oss.tsp.events.ScheduleStoreChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;

/**
 * Локальное хранилище данных о маршруте.
 *
 * Подписывается на событие обновления и обновляет свое состояние при получении события.
 */
public class ScheduleStore implements Store {
	private BehaviorSubject<List<Movement>> movementsSubject = BehaviorSubject.create();

	@Override
	public void start() {
		EventBus.getDefault().register(this);
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	public void onEvent(ScheduleEvent event) {
		movementsSubject.onNext(event.getMovements());
	}

	public Observable<List<Movement>> getObservable() {
		return movementsSubject;
	}

}
