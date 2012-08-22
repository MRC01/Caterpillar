/* Copyright 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
/* Manages all Point instances.
 * The purpose of this class is to eliminate new/free of Point objects.
 * This is because the lookahead algorithm creates billions of them,
 * which drives the JVM GC crazy.
 * The game has only a fixed number of positions, up to around 100x100.
 * So it's much better to create them all, cache and reuse them.
 * This class maintains a pool of Point instances unique by their coordinates.
 * NOTE: this class MUST NOT CREATE ANY OBJECTS when get() is called!
 *	Otherwise it defeats the purpose. This means Map cannot be used,
 *	since Map keys must be objects. That's why an array is used.
 * WARNING: each Point instance must be immutable!
 *	If its coordinates are changed, it will no longer match its cache position.
*/
package com.mrc.caterpillar;

import android.graphics.Point;

class PointFactory {
	/* Range is currently 7 bits for each coordinate.
	 * That makes a 14 bit range which is 16k instances.
	 * Assume Dalvik mem footprint for Point object is 32 bytes:
	 * 16 for Object, 4 for x, 4 for y, plus overhead.
	 * 16k instances @ 32 bytes each is about 1/2 MB of RAM - no problem.
	*/
	protected static final int	COORD_BITS = 7,
								COORD_MASK = 0x7F,
								COORD_RANGE = 0x4000;
	protected static GameConfig	gameCfg;
	public static PointFactory	self;

	int					segSize;
	protected Point[]	cache;
	protected Point		PointN, PointE, PointS, PointW;

	public static void init(int siz) {
		if(self == null) {
			gameCfg = CaterpillarMain.gameCfg;
			self = new PointFactory(siz);
		}
	}

	protected PointFactory(int siz) {
		segSize = siz;
		cache = new Point[COORD_RANGE];
		PointN = new Point(0, -segSize);
		PointE = new Point(segSize, 0);
		PointS = new Point(0, segSize);
		PointW = new Point(-segSize, 0);
	}

	// Gets the velocity point for the given direction
	public Point getVel(Direction dir) {
		Point	rc;
		
		switch(dir) {
		case N: rc = PointN;
			break;
		case E: rc = PointE;
			break;
		case S: rc = PointS;
			break;
		default: rc = PointW;
			break;
		}
		return rc;
	}

	// Gets the point that is the sum of the two given points
	public Point getSum(Point p1, Point p2) {
		return get(p1.x + p2.x, p1.y + p2.y);
	}

	// Gets the point having the given x & y coordinates
	public Point get(int x, int y) {
		int		k;
		Point	rc;
		k = key(x, y);
		rc = cache[k];
		if(rc == null) {
			rc = new Point(x, y);
			cache[k] = rc;
		}
		return rc;
	}

	/* Combines X & Y and returns a 12-bit int.
	 * X and Y are screen position pixel.
	 * Divide by seg size to get logical position, which greatly reduces the number of entries.
	*/
	protected int key(int x, int y) {
		int	k1, k2, rc;
		x = (x - gameCfg.XOFFSET) / segSize;
		y = (y - gameCfg.YOFFSET) / segSize;
		k1 = x & COORD_MASK;
		k2 = (y & COORD_MASK) << COORD_BITS;
		rc = (k1 | k2);
		return rc;
	}
}