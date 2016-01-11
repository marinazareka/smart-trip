package org.fruct.oss.tsp.mvp;

public interface Presenter<T> {
	void setView(T t);
	void start();
	void stop();
}
