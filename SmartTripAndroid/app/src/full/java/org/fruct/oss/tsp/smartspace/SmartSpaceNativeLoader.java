package org.fruct.oss.tsp.smartspace;

import org.fruct.oss.tsp.commondatatype.SmartSpaceNative;
import org.fruct.oss.tsp.smartslognative.JniSmartSpaceNative;

public class SmartSpaceNativeLoader {
	public static boolean loaded = false;
	public static SmartSpaceNative createSmartSpaceNative() {
		if (!loaded) {
			JniSmartSpaceNative.loadNativeLibrary();
			loaded = true;
		}

		return new JniSmartSpaceNative();
	}
}
