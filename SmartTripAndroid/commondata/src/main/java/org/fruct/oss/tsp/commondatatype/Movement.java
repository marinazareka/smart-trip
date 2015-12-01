package org.fruct.oss.tsp.commondatatype;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Отрезок пути маршурута
 */
public class Movement  implements Parcelable {
	private Point a;
	private Point b;

	public Movement(Point a, Point b) {
		this.a = a;
		this.b = b;
	}

	protected Movement(Parcel in) {
		a = in.readParcelable(Point.class.getClassLoader());
		b = in.readParcelable(Point.class.getClassLoader());
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(a, flags);
		dest.writeParcelable(b, flags);
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
