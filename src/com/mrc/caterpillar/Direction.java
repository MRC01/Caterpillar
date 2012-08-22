/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

public enum Direction {
	N, E, S, W;

	public Direction relativeDir(int dir) {
		return values()[((dir + ordinal()) % 4)];
	}
}
