/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.*;
import java.util.*;

import android.content.res.*;
import android.graphics.*;
import android.graphics.Paint.*;

import com.mrc.util.SoundShort;

/* This contains the game state and settings.
 * It's a singleton created by CaterpillarMain and shared by all the classes.
*/
public class GameConfig {
	// resources
	public static SoundShort	soundSpeedUp;
	public static Typeface		fontUbuntu;
	protected static Paint		paintBlack;
	static {
		paintBlack = new Paint();
		paintBlack.setColor(Color.BLACK);
		paintBlack.setStyle(Style.FILL);
	}

	static public void initResources(AssetManager am) throws IOException {
		if(soundSpeedUp == null) {
			soundSpeedUp = new SoundShort("speedUp.mp3", (float)0.8, am);
			fontUbuntu = Typeface.createFromAsset(am, "ubuntu.ttf");
		}
	}

	public static void adjustResources() {}

	public static void freeResources() {
		if(soundSpeedUp != null) {
			soundSpeedUp.close();
			soundSpeedUp = null;
			fontUbuntu = null;
		}
	}

	public class HighScore implements Comparable<HighScore> {
		String	sDate;
		String	sName;
		int		sScore;

		public HighScore() {
			sDate = "- never -";
			sName = "- empty -";
			sScore = 0;
		}

		public int compareTo(HighScore hs) {
			int i = 1;
			if(hs != null)
				if(sScore != hs.sScore) {
					if(sScore <= hs.sScore)
						i = -1;
				}
				else
					i = 0;
			return i;
		}
	}

	// screen stuff
	public static final float	ACCEL_1G			= 9.8F;
	public static final float	ACCEL_THRESHOLD		= (0.35F * ACCEL_1G);
	public static final String	APP_DIR				= ".caterpillar",
								HIGHSCORE_FILE		= "gameData.txt";
	public static final int		MAX_SCORES			= 5;
	public static final int		MAX_CATS			= 4;
	public static final int		STEP_DELAY_BASE		= 550;	// Base msecs for initial game speed
	public static final int		MAXTHINK			= 25,
								MAXLEN				= 10,
								MAXSPEED			= 5;
	public int					WIDTH, WIDTH_HALF, HEIGHT, XCELLS, YCELLS, XMAXPOS, YMAXPOS,
								SCREEN_X, SCREEN_Y, XOFFSET, YOFFSET;
	public double				ASPECT_RATIO;
	public int					SEG_SIZE;
	// game stuff
	public CatOption[]			catCfg				= new CatOption[MAX_CATS];
	public CtrlOption			ctrlOption;
	public CtrlInput			ctrlInput;
	public int					catLen;
	public int					catThink;
	public int					gameSpeed;
	public int					humanCatIdx;
	public long					speed_interval		= 15000, // How often to increase speed (msecs)
								stepDelay, 			// time interval for each step in the game
								min_step = 25;		// Fastest speed (minimum delay, msecs)
	public double				speed_step			= 0.1;	// speed increase increment (0.1 = 10%)
	volatile GameState			gameState;
	volatile Direction			humanCatInput;		// shared by the UI thread & game thread
	public List<HighScore>		highScores;
	public int					countApple = 1,
								chanceApple = 30,
								countClock = 1,
								chanceClock = 50,
								countLeaf = 2;

	public GameConfig() {
		catCfg[0] = CatOption.HUMAN;
		catCfg[1] = CatOption.NONE;
		catCfg[2] = CatOption.COMPUTER;
		catCfg[3] = CatOption.NONE;
		catLen = 5;
		catThink = 15;
		gameSpeed = 2;
		ctrlInput = CtrlInput.SWIPE;
		ctrlOption = CtrlOption.ABSOLUTE;
		gameState = GameState.preInit;
	}

	public void initScreen(int xSize, int ySize) {
		int		rem;

		SCREEN_X = xSize;
		SCREEN_Y = ySize;
		ASPECT_RATIO = ((double)xSize / (double)ySize);
		if(xSize * ySize <= (800*480))
			SEG_SIZE = 20;
		else if(xSize * ySize <= (1280*800))
			SEG_SIZE = 32;
		else
			SEG_SIZE = 40;
		PointFactory.init(SEG_SIZE);
		rem = xSize % SEG_SIZE;
		if(rem == 0) {
			xSize -= SEG_SIZE;
			XOFFSET = SEG_SIZE / 2;
		}
		else {
			xSize -= rem;
			XOFFSET = rem / 2;
		}
		rem = ySize % SEG_SIZE;
		if(rem == 0) {
			ySize -= SEG_SIZE;
			YOFFSET = SEG_SIZE / 2;
		}
		else {
			ySize -= rem;
			YOFFSET = rem / 2;
		}
		WIDTH = xSize;
		WIDTH_HALF = (int)(((double)xSize / 2.0) + 0.5) + XOFFSET;
		XCELLS = (xSize / SEG_SIZE);
		HEIGHT = ySize;
		YCELLS = (ySize / SEG_SIZE);
		XMAXPOS = (WIDTH - SEG_SIZE) + XOFFSET;
		YMAXPOS = (HEIGHT - SEG_SIZE) + YOFFSET;
	}

	// The GUI sets a value from 0 (slow) to 5 (fast).
	// Convert this to a time delay in milliseconds
	public void setGameSpeed() {
		stepDelay = STEP_DELAY_BASE - (gameSpeed * 100);
		if(stepDelay <= min_step)
			stepDelay = min_step;
	}

	public boolean shouldAccelerate(long ts1, long ts2, long step) {
		if(step <= min_step)
			return false;
		return(ts2 - ts1 > speed_interval);
	}

	// slows down the game by the given percentage
	public void slowGameSpeed(int pcnt) {
		long	step;

		step = (long)((double)stepDelay * ((double)pcnt / 100.0));
		stepDelay += step;
	}

	// updates game speed if necessary - usually does nothing
	// returns the tStart - usually unchanged
	protected long updateGameSpeed(long tStart, long ts1) {
		long	rc, step;

		rc = tStart;
		if(shouldAccelerate(tStart, ts1, stepDelay)) {
			// time to accelerate, so the new tStart is current timestamp t1
			soundSpeedUp.play();
			rc = ts1;
			step = (long)((double)stepDelay * speed_step);
			if(step <= 0)
				step = 1;
			stepDelay -= step;
			if(stepDelay <= min_step)
				stepDelay = min_step;
		}
		return rc;
	}
}
