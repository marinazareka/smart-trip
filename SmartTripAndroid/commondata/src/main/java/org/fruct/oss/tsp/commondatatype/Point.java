package org.fruct.oss.tsp.commondatatype;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Информация о географическом объекте
 */
public class Point implements Parcelable {
	private String id;
	private String title;
	private double lat;
	private double lon;

	public Point(Parcel parcel) {
		id = parcel.readString();
		title = parcel.readString();
		lat = parcel.readDouble();
		lon = parcel.readDouble();
	}

	public Point(String id, String title, double lat, double lon) {
		this.id = id;
		this.title = title;
		this.lat = lat;
		this.lon = lon;
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
	}
}
