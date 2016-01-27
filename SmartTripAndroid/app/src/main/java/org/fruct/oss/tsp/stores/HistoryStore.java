package org.fruct.oss.tsp.stores;

import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.events.HistoryEvent;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class HistoryStore implements Store {
	private BehaviorSubject<List<String>> subject = BehaviorSubject.create();

	@Override
	public void start() {
		EventBus.getDefault().register(this);
	}

	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(HistoryEvent event) {
		subject.onNext(event.getPatterns());
	}

	public Observable<List<String>> getObservable() {
		return subject;
	}
}
