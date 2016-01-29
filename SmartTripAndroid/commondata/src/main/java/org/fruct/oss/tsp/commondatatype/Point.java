package org.fruct.oss.tsp.commondatatype;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Информация о географическом объекте
 */
public class Point implements Parcelable, Serializable {
	private final String id;
	private final String title;
	private final double lat;
	private final double lon;

	private final boolean isPersisted;

	public Point(Parcel parcel) {
		id = parcel.readString();
		title = parcel.readString();
		lat = parcel.readDouble();
		lon = parcel.readDouble();
		isPersisted = parcel.readInt() != 0;
	}

	public Point(String id, String title, double lat, double lon) {
		this(id, title, lat, lon, false);
	}

	public Point(String id, String title, double lat, double lon, boolean isPersisted) {
		this.id = id;
		this.title = title;
		this.lat = lat;
		this.lon = lon;
		this.isPersisted = isPersisted;
	}

	/**
	 *
	 * @return Уникальный идентификатор точки
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Широта
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @return Долгота
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @return Название точки
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Флаг сохранения точки в локальноц БД
	 * @return true если сохранена, false, если нет
	 */
	public boolean isPersisted() {
		return isPersisted;
	}

	public static final Creator<Point> CREATOR = new Creator<Point>() {
		@Override
		public Point createFromParcel(Parcel source) {
			return new Point(source);
		}

		@Override
		public Point[] newArray(int size) {
			return new Point[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(title);
		dest.writeDouble(lat);
		dest.writeDouble(lon);
		dest.writeInt(isPersisted ? 1 : 0);
	}

	public static void save(@Nullable Point point, Bundle bundle, String key) {
		bundle.putParcelable(key, point);
	}

	@Nullable
	public static Point restore(Bundle bundle, String key) {
		return bundle.getParcelable(key);
	}
}
