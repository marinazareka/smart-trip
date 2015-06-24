package org.fruct.oss.smarttrip.util;

import org.fruct.oss.smarttrip.jni.Smart;

public class Test {
	static {
		System.loadLibrary("Smart");
	}

	public static boolean test() {
		return Smart.connect("X", "192.168.1.20", 10622);
	}
}
