/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import android.content.*;
import android.graphics.*;
import android.view.*;

/* This class runs and draws the actual game; it is created by the CaterpillarGame Activity.
 * It is a SurfaceView for drawing the screen Canvas,
 * and it is a Thread for running the game independently of the main Android thread,
 * which gets the UI (touch events & accelerometer sensor).
*/
class GameBoard extends SurfaceView implements Runnable {
	public static GameBoard	self;

	Cat[]					cats;
	CaterpillarGame			parentGameActivity;
	GameConfig				gameCfg;
	Thread					gameThread	= null;
	SurfaceHolder			sfcHold;
	volatile boolean		quit;

	public GameBoard(Context _ctx) {
		super(_ctx);
		parentGameActivity = (CaterpillarGame)_ctx;
		gameCfg = CaterpillarMain.gameCfg;
		sfcHold = getHolder();
	}

	public void resume() {
		self = this;
		quit = false;
		gameCfg.gameState = GameState.inGame;
		gameThread = new Thread(this);
		gameThread.start();
	}

	public void pause() {
		quit = true;
		gameCfg.gameState = GameState.gameOver;
		while(true) {
			if(!gameThread.isAlive())
				return;
			try {
				gameThread.join();
			}
			catch(InterruptedException localInterruptedException) {
				Util.sleep();
			}
		}
	}

	// The main game thread
	// When the game is over this thread draws the "game over" screen and exits 
	public void run() {
		int		hiScoreIdx = -1;
		long	tStart, ts1, ts2, dt;
		Canvas	cvs;

		// initialize the game
		while(!sfcHold.getSurface().isValid()) {
			// don't hog CPU while waiting
			Util.sleep();
			continue;
		}
		cvs = sfcHold.lockCanvas();
		try {
			initGame(cvs);
		}
		finally {
			sfcHold.unlockCanvasAndPost(cvs);
		}

		// main game loop
		startGame();
		tStart = System.currentTimeMillis();
		while(gameCfg.gameState == GameState.inGame) {
			ts1 = System.currentTimeMillis();
			// run one iteration of the game
			gameStep();
			// draw the screen
			cvs = sfcHold.lockCanvas();
			try {
				drawScreen(cvs);
				if(gameCfg.gameState == GameState.gameOver) {
					hiScoreIdx = gameOver(cvs);
					break;
				}
			}
			finally {
				sfcHold.unlockCanvasAndPost(cvs);
			}
			// Wait delay for game timing
			if(gameCfg.gameState == GameState.inGame) {
				// gradually accelerate the game
				tStart = gameCfg.updateGameSpeed(tStart, ts1);
				// wait (if necessary) for real-time pacing
				ts2 = System.currentTimeMillis();
				dt = ts2 - ts1;
				if(dt < gameCfg.stepDelay)
					Util.sleep(gameCfg.stepDelay - dt);
			}
		}
		// game has ended
		if((!quit) && (hiScoreIdx >= 0)) {
			// User got a high score, should enter his name 
			// NOTE: this opens a dialog activity, which pauses & resumes my parent activity
			enterHighScoreName(hiScoreIdx);
		}
	}

	protected void initGame(Canvas cvs) {
		int		x, y;
		gameCfg.gameState = GameState.preInit;
		x = cvs.getWidth();
		y = cvs.getHeight();
		gameCfg.initScreen(x, y);
		gameCfg.setGameSpeed();
		CaterpillarMain.adjustResources();
	}

	public void startGame() {
		TargetFactory.clear();
		cats = new Cat[4];
		gameCfg.humanCatIdx = -1;
		for(int i = 0; i < 4; i++)
			startCat(i);
		gameCfg.gameState = GameState.inGame;
	}

	public void gameStep() {
		int i = 0;
		for(int j = 0; j < 4; j++) {
			if(cats[j] == null)
				continue;
			if(cats[j].fullMove().isDead())
				i = 1;
		}
		if(i != 0)
			gameCfg.gameState = GameState.gameOver;
		TargetFactory.update();
	}

	public void drawScreen(Canvas cvs) {
		drawBackground(cvs);
		TargetFactory.drawAll(cvs);
		for(int i = 0; i < 4; i++) {
			if(cats[i] != null)
				cats[i].draw(cvs);
		}
	}

	public void drawBackground(Canvas cvs) {
		// wipe the entire screen to the boundary color
		cvs.drawRGB(0, 0xFF, 0);
		// clear the black area inside boundary rectangle to the screen background color
		cvs.drawRect(gameCfg.XOFFSET + 1, gameCfg.YOFFSET + 1,
				gameCfg.SCREEN_X - gameCfg.XOFFSET - 1, gameCfg.SCREEN_Y - gameCfg.YOFFSET - 1,
				gameCfg.paintBlack);
	}

	public void startCat(int which) {
		int			len, think;
		Direction	dir;
		if(gameCfg.catCfg[which] != CatOption.NONE) {
			dir = Direction.values()[which];
			len = gameCfg.catLen;
			think = gameCfg.catThink;
			boolean bool;
			if(gameCfg.catCfg[which] != CatOption.COMPUTER)
				bool = false;
			else
				bool = true;
			Cat.Config localConfig = new Cat.Config(which, dir, len, think, bool);
			cats[which] = new Cat(localConfig);
		}
		else {
			cats[which] = null;
		}
		if(CatOption.HUMAN == gameCfg.catCfg[which])
			gameCfg.humanCatIdx = which;
	}

	// Returns index of user in high score list, -1 if not a high score
	public int gameOver(Canvas cvs) {
		int		hiScoreIdx,
				rc = -1;

		Paint localPaint = new Paint();
		int i = 3 * gameCfg.SEG_SIZE;
		printString("G A M E	 O V E R", cvs, i);
		i += 2 * gameCfg.SEG_SIZE;
		printString("Status:", cvs, i);
		int yPos = i + 2 * gameCfg.SEG_SIZE;
		for(int whichCat = 0; whichCat < gameCfg.MAX_CATS; whichCat++) {
			Cat cat = cats[whichCat];
			if(cat == null)
				continue;
			int j = gameCfg.WIDTH / 2 - 8 * gameCfg.SEG_SIZE;
			int i2 = (int)(0.7D * gameCfg.SEG_SIZE);
			cvs.drawBitmap(
					Cat.ourResources[cat.cfg.id].headImg[1], j, yPos - i2,
					null);
			if(cat.isDead) {
				localPaint.setColor(Color.RED);
				localPaint.setStrokeWidth(3);
				cvs.drawLine(j, yPos - i2, j + gameCfg.SEG_SIZE, yPos - i2 + gameCfg.SEG_SIZE, localPaint);
				cvs.drawLine(j, yPos - i2 + gameCfg.SEG_SIZE, j + gameCfg.SEG_SIZE, yPos - i2, localPaint);
			}
			hiScoreIdx = CaterpillarScore.checkHighScore(cat.score);
			printString("Score " + Util.stringFromInt(cat.score)
						+ " - Length " + Util.stringFromInt(cat.length),
					cvs, j + 2 * gameCfg.SEG_SIZE, yPos);
			yPos += gameCfg.SEG_SIZE;
			if((hiScoreIdx >= 0) && (gameCfg.humanCatIdx == whichCat)) {
				rc = hiScoreIdx;
			}
		}
		return rc;
	}

	// Launches a dialog to handle adding a user to the High Score list
	public void enterHighScoreName(int hsIdx) {
		Intent	launchDialog;

		parentGameActivity.inHighScoreDialog();
		launchDialog = new Intent(parentGameActivity, HighScoreName.class);
		// pass data to the activity in the form of KV pairs
		launchDialog.putExtra("index", hsIdx);
		parentGameActivity.startActivity(launchDialog);
	}

	public void printString(String str, Canvas cvs, int y) {
		printString(str, cvs, -1, y);
	}

	public void printString(String str, Canvas cvs, int x, int y) {
		Paint localPaint = new Paint();
		localPaint.setTypeface(GameConfig.fontUbuntu);
		localPaint.setTextSize(gameCfg.SEG_SIZE);
		localPaint.setColor(-1);
		if(x != -1) {
			localPaint.setTextAlign(Paint.Align.LEFT);
		}
		else {
			x = gameCfg.WIDTH / 2;
			localPaint.setTextAlign(Paint.Align.CENTER);
		}
		cvs.drawText(str, x, y, localPaint);
	}
}
