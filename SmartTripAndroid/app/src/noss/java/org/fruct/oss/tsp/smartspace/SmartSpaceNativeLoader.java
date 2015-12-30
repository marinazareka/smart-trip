package org.fruct.oss.tsp.smartspace;

import org.fruct.oss.tsp.commondatatype.SmartSpaceNative;

public class SmartSpaceNativeLoader {
	public static SmartSpaceNative createSmartSpaceNative() {
		return new TestSmartSpaceNative();
	}
}
