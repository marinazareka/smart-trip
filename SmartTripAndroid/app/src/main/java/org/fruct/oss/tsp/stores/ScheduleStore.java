package org.fruct.oss.tsp.stores;

import android.os.Environment;

import org.fruct.oss.tsp.App;
import org.fruct.oss.tsp.commondatatype.Movement;
import org.fruct.oss.tsp.events.ScheduleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Локальное хранилище данных о маршруте.
 *
 * Подписывается на событие обновления и обновляет свое состояние при получении события.
 */
public class ScheduleStore implements Store {
	private static final Logger log = LoggerFactory.getLogger(ScheduleStore.class);

	private BehaviorSubject<List<Movement>> movementsSubject = BehaviorSubject.create();

	private MovementsPersist movementsPersist;

	@Override
	public void start() {
		restoreMovements();

		if (movementsPersist != null) {
			movementsSubject.onNext(movementsPersist.getMovements());
		}

		EventBus.getDefault().register(this);
	}


	@Override
	public void stop() {
		EventBus.getDefault().unregister(this);
		storeMovements();
	}

	private void restoreMovements() {
		File cacheDir = App.getContext().getCacheDir();
		File file = new File(cacheDir, "movements.serialized");
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(file));
			movementsPersist = (MovementsPersist) in.readObject();
			log.debug("Movements deserialized successfully");
		} catch (Exception e) {
			log.warn("Can't deserialize previous movements");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) { }
		}
	}

	private void storeMovements() {
		File cacheDir = App.getContext().getCacheDir();
		File file = new File(cacheDir, "movements.serialized");

		if (movementsPersist == null) {
			file.delete();
			return;
		}

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(movementsPersist);
			log.debug("Movements serialized successfully");
		} catch (Exception e) {
			log.warn("Can't serialize movements");
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) { }
		}
	}

	public void onEvent(ScheduleEvent event) {
		movementsPersist = new MovementsPersist(event.getMovements());
		movementsSubject.onNext(event.getMovements());
	}

	public Observable<List<Movement>> getObservable() {
		return movementsSubject;
	}

	private static class MovementsPersist implements Serializable {
		private ArrayList<Movement> movements;

		public MovementsPersist(List<Movement> movements) {
			this.movements = new ArrayList<>(movements);
		}

		public ArrayList<Movement> getMovements() {
			return movements;
		}
	}
}
