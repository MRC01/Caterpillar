/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.*;

/* This class represents the yummy Apple that hungry Caterpillars like to eat.
 * It's like TargetLeaf, but grows the caterpillar more, and lasts for a shorter time.
*/
public class TargetApple extends Target {
	protected static final int		MAX_POINTS = 20;
	protected static final String	imageFilename = "apple.png",
									soundFilename = "apple.mp3";
	protected static int			instanceCount = 0;
	protected static Bitmap			ourImgDefault;
	protected static Sound			ourStartSound;

	// WARNING: you must call this before you instantiate the class
	static public void initResources(AssetManager am) throws IOException {
		if(ourImgDefault == null) {
			ourImgDefault = Util.getBitmap(imageFilename, am);
			ourStartSound = new Sound(soundFilename, (float)1.0, am);
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
		}
	}

	public TargetApple(Point _pos) {
		super(_pos, MAX_POINTS);
		lifespanBase = 4;
		ourStartSound.play();
		img = ourImgDefault;
	}
	
	protected void subEat(Cat cat) {
		Cat.ourResources[cat.cfg.id].soundEat.play();
		cat.growing += points;
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
