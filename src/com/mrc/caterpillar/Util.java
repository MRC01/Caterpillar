/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import android.graphics.*;

// This is the home of various utility functions.
public class Util extends com.mrc.util.Util {
	static {
		APP_DIR = GameConfig.APP_DIR;
	}

	public static Point randomPos() {
		return PointFactory.self.get(
			(int)(Math.random()*CaterpillarMain.gameCfg.XCELLS) * CaterpillarMain.gameCfg.SEG_SIZE + CaterpillarMain.gameCfg.XOFFSET,
			(int)(Math.random()*CaterpillarMain.gameCfg.YCELLS) * CaterpillarMain.gameCfg.SEG_SIZE + CaterpillarMain.gameCfg.YOFFSET);
	}
	
	public static String stringFromInt(int val) {
		return String.format("%,d", val);
	}
}
