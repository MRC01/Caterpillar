/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.*;

import android.content.res.*;
import android.graphics.*;
import android.os.*;

// This is the home of various utility functions.
public class Util {
	static File			extDir	= null,
						appDir	= null;

	public static Bitmap getBitmap(String fName, AssetManager am)
			throws IOException {
		return BitmapFactory.decodeStream(am.open(fName));
	}

	protected static File getFile(String fName) {
		File fil = initFiles();
		if(fil != null)
			fil = new File(fil.getAbsolutePath() + File.separator + fName);
		else
			fil = null;
		return fil;
	}

	// gets this app's home directory, creates if necessary
	protected static File initFiles() {
		if(appDir == null) {
			if((extDir == null) && ("mounted".equals(Environment.getExternalStorageState())))
				extDir = Environment.getExternalStorageDirectory();
			if(extDir != null)
				appDir = new File(extDir.getAbsolutePath() + File.separator + GameConfig.APP_DIR);
			if(!appDir.exists())
				appDir.mkdirs();
		}
		return appDir;
	}

	public static BufferedReader readFile(String fName) throws IOException {
		return new BufferedReader(new FileReader(getFile(fName)));
	}

	public static BufferedWriter writeFile(String fName) throws IOException {
		return new BufferedWriter(new FileWriter(getFile(fName)));
	}

	// WARNING: assumes the bitmap is square
	public static Bitmap resizeBitmap(Bitmap bm, int newSiz) {
		if(bm.getWidth() != newSiz)
			bm = Bitmap.createScaledBitmap(bm, newSiz, newSiz, false);
		return bm;
	}

	public static int random(int range) {
		return (int)(Math.random() * (double)range);
	}
	
	public static void sleep() {
		sleep(0L);
	}

	public static void sleep(long msecs) {
		try {
			Thread.sleep(msecs);
		}
		catch(InterruptedException localInterruptedException) {
			// do nothing - we don't care
		}
	}

	public static Point randomPos() {
		return new Point(
			(int)(Math.random()*CaterpillarMain.gameCfg.XCELLS) * CaterpillarMain.gameCfg.SEG_SIZE + CaterpillarMain.gameCfg.XOFFSET,
			(int)(Math.random()*CaterpillarMain.gameCfg.YCELLS) * CaterpillarMain.gameCfg.SEG_SIZE + CaterpillarMain.gameCfg.YOFFSET);
	}
	
	public static String stringFromInt(int val) {
		return String.format("%,d", val);
	}
}
