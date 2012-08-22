/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

public enum DirAxis {
	HORIZONTAL ("Horizontal"),
	VERTICAL ("Vertical");
	public final String	itsName;
	DirAxis(String nam) {
		itsName = nam;
	}
	public static DirAxis fromName(String text) {
		if (text != null) {
			for (DirAxis v : DirAxis.values()) {
				if (text.equalsIgnoreCase(v.itsName))
					return v;
			}
		}
		throw new IllegalArgumentException("No enum of name " + text);
	}
}
