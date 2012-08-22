/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

//NOTE: must match string-array ctrl_inputs in strings.xml
public enum CtrlInput {
	SWIPE ("Swipe"),
	TOUCH ("Touch"),
	ACCELEROMETER ("Accelerometer");
	public final String	itsName;
	CtrlInput(String nam) {
		itsName = nam;
	}
	public static CtrlInput fromName(String text) {
		if (text != null) {
			for (CtrlInput v : CtrlInput.values()) {
				if (text.equalsIgnoreCase(v.itsName))
					return v;
			}
		}
		throw new IllegalArgumentException("No enum of name " + text);
	}
}

