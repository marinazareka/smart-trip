package org.fruct.oss.tsp.commondatatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Отрезок пути маршурута
 */
public class Movement  implements Parcelable, Serializable {
	private final Point a;
	private final Point b;

	@Nullable
	private final DateTime startDateTime;

	@Nullable
	private final DateTime endDateTime;

	@Deprecated
	public Movement(Point a, Point b) {
		this(a, b, null, null);
	}

	public Movement(Point a, Point b, @Nullable String startDateTimeIso, @Nullable  String endDateTimeIso) {
		this.a = a;
		this.b = b;

		if (!TextUtils.isEmpty(startDateTimeIso)) {
			startDateTime = DateTime.parse(startDateTimeIso);
		} else {
			startDateTime = null;
		}

		if (!TextUtils.isEmpty(endDateTimeIso)) {
			endDateTime = DateTime.parse(endDateTimeIso);
		} else {
			endDateTime = null;
		}
	}

	protected Movement(Parcel in) {
		a = in.readParcelable(Point.class.getClassLoader());
		b = in.readParcelable(Point.class.getClassLoader());
		startDateTime = (DateTime) in.readSerializable();
		endDateTime = (DateTime) in.readSerializable();
	}

	/**
	 * @return Начальная точка маршрута
	 */
	public Point getA() {
		return a;
	}

	/**
	 * @return Конечная точка маршрута
	 */
	public Point getB() {
		return b;
	}

	@Nullable
	public DateTime getStartDateTime() {
		return startDateTime;
	}

	@Nullable
	public DateTime getEndDateTime() {
		return endDateTime;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(a, flags);
		dest.writeParcelable(b, flags);
		dest.writeSerializable(startDateTime);
		dest.writeSerializable(endDateTime);
	}

	public static final Creator<Movement> CREATOR = new Creator<Movement>() {
		@Override
		public Movement createFromParcel(Parcel in) {
			return new Movement(in);
		}

		@Override
		public Movement[] newArray(int size) {
			return new Movement[size];
		}
	};
}
