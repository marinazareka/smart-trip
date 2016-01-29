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
	private final long localId;
	private final String remoteId;
	private final String title;
	private final double lat;
	private final double lon;

	public Point(Parcel parcel) {
		localId = parcel.readLong();
		remoteId = parcel.readString();
		title = parcel.readString();
		lat = parcel.readDouble();
		lon = parcel.readDouble();
	}

	public Point(String remoteId, String title, double lat, double lon) {
		this(-1, remoteId, title, lat, lon);
	}

	public Point(long id, String remoteId, String title, double lat, double lon) {
		this.localId = id;
		this.remoteId = remoteId;
		this.title = title;
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * @return Уникальный идентификатор точки в локальной базе данных
	 */
	public long getLocalId() {
		return localId;
	}

	/**
	 * @return Уникальный идентификатор точки в smartspace
	 */
	public String getRemoteId() {
		return remoteId;
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
		return localId >= 0;
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
		dest.writeLong(localId);
		dest.writeString(remoteId);
		dest.writeString(title);
		dest.writeDouble(lat);
		dest.writeDouble(lon);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Point point = (Point) o;

		if (Double.compare(point.lat, lat) != 0) return false;
		if (Double.compare(point.lon, lon) != 0) return false;
		return title.equals(point.title);

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = title.hashCode();
		temp = Double.doubleToLongBits(lat);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public static void save(@Nullable Point point, Bundle bundle, String key) {
		bundle.putParcelable(key, point);
	}

	@Nullable
	public static Point restore(Bundle bundle, String key) {
		return bundle.getParcelable(key);
	}
}
