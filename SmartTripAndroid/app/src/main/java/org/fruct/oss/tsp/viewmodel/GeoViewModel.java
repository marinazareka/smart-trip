package org.fruct.oss.tsp.viewmodel;

import org.fruct.oss.tsp.commondatatypes.Point;

import java.util.List;

/**
 * Модель представления для фрагмента географических объектов. Хранит состояние выбора пользователем точек,
 * а также обновляет список при обновлении хранилища с сохранением статуса "выбрано".
 */
public interface GeoViewModel {
	/**
	 * Зарегистрировать listener для уведомления об изменении данных
	 * @param listener {@link org.fruct.oss.tsp.viewmodel.GeoViewModel.Listener}
	 */
	void registerListener(Listener listener);
	void unregisterListener(Listener listener);

	void start();
	void stop();

	/**
	 * Получить список точек
	 * @return список точек
	 */
	List<PointModel> getPoints();

	/**
	 * @return true если хотя бы одна точка выбрана
	 */
	boolean isAnythingChecked();

	/**
	 * Установить состояние выбора
	 * @param position позиция точки в списке
	 * @param checked состояние выбрано/не выбрано
	 */
	void setCheckedState(int position, boolean checked);

	interface Listener {
		void pointsUpdated(List<PointModel> points);
	}

	/**
	 * Элемент списка точек
	 */
	class PointModel {
		/**
		 * Состояние выбора точки (выбрано/не выбрано)
		 */
		public boolean isChecked;
		/**
		 * Объект {@link Point}
		 */
		public Point point;
	}
}
