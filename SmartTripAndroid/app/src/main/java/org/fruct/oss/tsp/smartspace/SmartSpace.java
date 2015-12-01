package org.fruct.oss.tsp.smartspace;

import android.location.Location;

import org.fruct.oss.tsp.commondatatypes.Point;
import org.fruct.oss.tsp.data.ScheduleRequest;
import org.fruct.oss.tsp.data.SearchRequest;
import org.fruct.oss.tsp.data.User;

import java.util.List;

/**
 * Интерфейс для работы с интеллектуальным пространством
 */
public interface SmartSpace {
	/**
	 * Публикация данных о пользователе
	 *
	 * Метод проверяет наличие пользователя в интеллектуальном пространстве и при необходимости обновляет его
	 * @param user пользователь
	 */
	void publishUser(User user);

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
	 * @return объект-идентификатор запроса
	 */
	SearchRequest postSearchRequest(double radius, String pattern);

	/**
	 * Опубликовать запрос на построение маршрута
	 *
	 * Результат запроса будет получен асинхронно в виде события {@link org.fruct.oss.tsp.events.ScheduleEvent}
	 * @param pointList Список точек, по которым необходимо построить маршрут
	 * @return объект-идентификатор запроса
	 */
	ScheduleRequest postScheduleRequest(List<Point> pointList);
}
