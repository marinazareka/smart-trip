package org.fruct.oss.smarttrip.util;

import org.fruct.oss.smarttrip.jni.TestLib;

public class Test {
	static {
		System.loadLibrary("TestLib");
	}

	public static boolean test() {
		return TestLib.test("X", "172.20.2.240", 10622);
	}
}
