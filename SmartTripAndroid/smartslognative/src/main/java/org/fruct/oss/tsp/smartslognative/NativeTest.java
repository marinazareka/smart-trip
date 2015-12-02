package org.fruct.oss.tsp.smartslognative;

public class NativeTest {
	static {
		System.loadLibrary("smartslognative");
	}
	public static native String helloworld();
	public static native int divide(int a, int b);
}
