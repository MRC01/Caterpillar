/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.*;

import com.mrc.util.SoundShort;

/* This class represents the leaf that hungry Caterpillars like to eat.
 * It appears at a random location, lives for a random length of time,
 * and when eaten it grows the Caterpillar a random amount.
 * As it sits on the screen, it goes "stale" and has less "energy",
 * so the sooner it is eaten the more the Caterpillar grows.
*/
public class TargetLeaf extends Target {
	protected static final int		MAX_POINTS = 5;
	protected static final String	imageFilename = "leaf.png",
									soundFilename = "appearPop.mp3";
	protected static int			instanceCount = 0;
	protected static Bitmap			ourImgDefault;
	protected static SoundShort		ourStartSound;

	// WARNING: you must call this before you instantiate the class
	public static void initResources(AssetManager am) throws IOException {
		if(ourImgDefault == null) {
			ourImgDefault = Util.getBitmap(imageFilename, am);
			ourStartSound = new SoundShort(soundFilename, (float)1.0, am);
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

	public TargetLeaf(Point _pos) {
		super(_pos, MAX_POINTS);
		lifespanBase = 10;
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
