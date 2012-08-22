/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.*;

import android.media.*;
import android.content.res.*;

/* This class facilitates the use of sound effects.
 * All sounds share a common static Pool used to play them.
 * To use sound, simply instantiate one giving its filename,
 * call play() to play it, close() to free its resources.
*/
public class Sound {
	static protected SoundPool	ourPool = new SoundPool(12, AudioManager.STREAM_MUSIC, 0);
	static final float			VOL_DEFAULT = (float)1.0;

	protected AssetFileDescriptor	fDes;
	protected int					fId;
	protected float					vol;
	public int						repeat;

	public Sound(String _fName, AssetManager am) throws IOException {
		init(_fName, VOL_DEFAULT, am);
	}
	
	public Sound(String _fName, float _vol, AssetManager am) throws IOException {
		init(_fName, _vol, am);
	}
	
	protected void init(String _fName, float _vol, AssetManager am) throws IOException {
		repeat = 0;
		vol = _vol;
		fDes = am.openFd(_fName);
		fId = ourPool.load(fDes, 1);
	}
	
	public void close() {
		ourPool.unload(fId);
	}

	public void play() {
		ourPool.play(fId, vol, vol, 0, repeat, 1);
	}

	public void stop() {
		ourPool.stop(fId);
	}
}
