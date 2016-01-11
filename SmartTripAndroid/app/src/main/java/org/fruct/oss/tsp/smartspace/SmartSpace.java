package org.fruct.oss.tsp.smartspace;

import android.location.Location;

import org.fruct.oss.tsp.commondatatype.Point;
import org.fruct.oss.tsp.commondatatype.TspType;

import java.util.List;

/**
 * Интерфейс для работы с интеллектуальным пространством
 */
public interface SmartSpace {
	/**
	 * Обновление местоположения пользователя
	 *
	 * Обновление может привести к загрузке новых данных при наличии активного запроса
	 * @param location местоположение пользователя
	 */
	void updateUserLocation(Location location);

	/**
	 * Опубликовать запрос на загрузку точек
	 *
	 * Результат запроса будет получен асинхронно в виде события {@link org.fruct.oss.tsp.events.SearchEvent}
	 * @param radius радиус поиска в метрах
	 * @param pattern строка-шаблон поиска
	 */
	void postSearchRequest(double radius, String pattern);

	/**
	 * Опубликовать запрос на построение маршрута
	 *
	 * Результат запроса будет получен асинхронно в виде события {@link org.fruct.oss.tsp.events.ScheduleEvent}
	 * @param pointList Список точек, по которым необходимо построить маршрут
	 */
	void postScheduleRequest(List<Point> pointList, TspType tspType);
}
