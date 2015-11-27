package org.fruct.oss.tsp.data;

/**
 * Данные пользователя
 */
public class User {
	private String id;

	public User(String id) {
		this.id = id;
	}

	/**
	 * @return Уникальный идентификатор пользователя
	 */
	public String getId() {
		return id;
	}
}
