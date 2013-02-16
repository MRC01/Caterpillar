/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.*;

import com.mrc.util.SoundShort;

/* This class represents the clock.
 * It appears at a random location, lives for a random length of time,
 * and when eaten it slows down the game.
*/
public class TargetClock extends Target {
	protected static final int		MAX_POINTS = 20;
	protected static final String	imageFilename = "clock.png",
									soundStartFilename = "clockStart.mp3",
									soundEatFilename = "clockEat.mp3";
	protected static int			instanceCount = 0;
	protected static Bitmap			ourImgDefault;
	protected static SoundShort		ourStartSound, ourEatSound;

	// WARNING: you must call this before you instantiate the class
	public static void initResources(AssetManager am) throws IOException {
		if(ourImgDefault == null) {
			ourImgDefault = Util.getBitmap(imageFilename, am);
			ourStartSound = new SoundShort(soundStartFilename, (float)1.0, am);
			ourEatSound = new SoundShort(soundEatFilename, (float)1.0, am);
		}
	}

	public static void adjustResources() {
		ourImgDefault = Util.resizeBitmap(ourImgDefault, CaterpillarMain.gameCfg.SEG_SIZE);
	}

	public static void freeResources() {
		if(ourImgDefault != null) {
			ourImgDefault.recycle();
			ourImgDefault = null;
			ourStartSound.close();
			ourStartSound = null;
			ourEatSound.close();
			ourEatSound = null;
		}
	}

	public TargetClock(Point _pos) {
		super(_pos, MAX_POINTS);
		lifespanBase = 8;
		ourStartSound.play();
		img = ourImgDefault;
	}

	protected void subEat(Cat cat) {
		ourEatSound.play();
		CaterpillarMain.gameCfg.slowGameSpeed(points);
	}
	
	protected static void countZero() {
		instanceCount = 0;
	}
	
	protected void countInc() {
		instanceCount++;
	}
	
	protected void countDec() {
		instanceCount--;
	}
}
