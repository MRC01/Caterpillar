/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.IOException;

import android.app.*;
import android.content.*;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.*;
import android.view.*;

import com.mrc.util.SoundLong;

/* This is the game's primary Activity, main screen, and entry point.
 * It is a useful placeholder for game global stuff, like GameConfig.
*/
public class CaterpillarMain extends Activity {
	public static GameConfig	gameCfg	= new GameConfig();
	protected static boolean	resourcesInit = false,
								resourcesAdjusted = false;
	protected static String		ourMusicFilename = "sonataK545Mozart.mid";
	static SoundLong			ourMusic = null;

	protected void initResources() throws IOException {
		if(!resourcesInit) {
			resourcesInit = true;
			AssetManager am = getAssets();
			GameConfig.initResources(am);
			Cat.initResources(am);
			TargetApple.initResources(am);
			TargetLeaf.initResources(am);
			TargetClock.initResources(am);
			ourMusic = new SoundLong(ourMusicFilename, (float)0.7, true, am);
		}
	}

	public static void adjustResources() {
		if(!resourcesAdjusted) {
			resourcesAdjusted = true;
			GameConfig.adjustResources();
			Cat.adjustResources();
			TargetApple.adjustResources();
			TargetLeaf.adjustResources();
			TargetClock.adjustResources();
		}
	}

	protected static void freeResources() {
		resourcesAdjusted = false;
		resourcesInit = false;
		ourMusic.close();
		TargetClock.freeResources();
		TargetLeaf.freeResources();
		TargetApple.freeResources();
		Cat.freeResources();
		GameConfig.freeResources();
	}

	public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(
    			WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        // Init resources, load high scores, etc.
		try {
			initResources();
		}
		catch(Exception e) {
			// TODO: could not load resources
		}
		try {
			CaterpillarScore.loadScores();
		}
		catch(Exception e) {
			// TODO: could not load high scores
		}
		setContentView(R.layout.mainlayout);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
		if(isFinishing()) {
			// Game is exiting - write high scores & free resources
			try {
				CaterpillarScore.writeScores();
			}
			catch(Exception e) {}
			freeResources();
		}
	}

	public void butGameSetup(View paramView) {
		startActivity(new Intent(this, CaterpillarSetup.class));
	}

	public void butGameScores(View paramView) {
		startActivity(new Intent(this, CaterpillarScore.class));
	}

	public void butGameStart(View paramView) {
		startGame();
	}

	public void butGameQuit(View paramView) {
		finish();
	}

	protected void startGame() {
		startActivity(new Intent(this, CaterpillarGame.class));
	}
}
