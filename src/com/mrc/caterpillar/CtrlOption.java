/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

// NOTE: must match string-array ctrl_options in strings.xml
public enum CtrlOption {
	ABSOLUTE ("Absolute"),
	RELATIVE ("Relative");
	public final String	itsName;
	CtrlOption(String nam) {
		itsName = nam;
	}
	public static CtrlOption fromName(String text) {
		if (text != null) {
			for (CtrlOption v : CtrlOption.values()) {
				if (text.equalsIgnoreCase(v.itsName))
					return v;
			}
		}
		throw new IllegalArgumentException("No enum of name " + text);
	}
}
