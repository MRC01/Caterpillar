/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.util.*;

import android.app.*;
import android.graphics.*;
import android.hardware.*;
import android.os.*;
import android.view.*;
import android.view.View.*;

/* This is the screen for playing the game.
 * It creates a "GameBoard" to draw and run the game, which has its own thread.
 * Meanwhile, this screen has the main Android UI thread,
 * accepts user input that is applied by the GameBoard thread.
*/
public class CaterpillarGame extends Activity
		implements OnTouchListener, SensorEventListener {
	GameBoard		gameBoard;
	GameConfig		gameCfg;
	Point			pDown	= new Point();
	Point			pUp		= new Point();
	Sensor			snsAccel;
	SensorManager	snsMgr;
	boolean			inHighScoreDialog = false;

	public void onCreate(Bundle savedInstanceState) {
		List<Sensor>	snsList;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(
    			WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	getWindow().setFlags(
    			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
    			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
		gameCfg = CaterpillarMain.gameCfg;
		// CAUTION: this code can cause the emulator to hang indefinitely
		snsMgr = ((SensorManager)getSystemService("sensor"));
		snsList = snsMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if((snsList != null) && (!snsList.isEmpty()))
			snsAccel = snsList.get(0);
	}

	protected void onPause() {
		super.onPause();
		gameBoard.pause();
		if(snsMgr != null)
			snsMgr.unregisterListener(this);
	}

	/* Normally, "resume" means we're starting the game.
	 * But it could mean we are returning from entering a new name for a high score.
	 * In this latter case, don't start up a game.
	 */
	protected void onResume() {
		super.onResume();
		if(inHighScoreDialog) {
			inHighScoreDialog = false;
			return;
		}
		if(snsAccel != null)
			snsMgr.registerListener(this, snsAccel, SensorManager.SENSOR_DELAY_GAME);
		gameBoard = new GameBoard(this);
		gameBoard.setOnTouchListener(this);
		setContentView(gameBoard);
		gameBoard.resume();
	}

	public void inHighScoreDialog() {
		// There is no method to turn this off.
		// It happens automatically after the dialog is dismissed when this activity resumes. 
		inHighScoreDialog = true;
	}

	// Never used, but must implement for SensorEventListener
	public void onAccuracyChanged(Sensor sensor, int val) { }

	public void onSensorChanged(SensorEvent evt) {
		// ignore accelerometer if using touch controls
		if(gameCfg.ctrlInput == CtrlInput.ACCELEROMETER)
			setDirAccel(evt.values[0], evt.values[1], evt.values[2]);
	}

	public boolean onTouch(View v, MotionEvent evt) {
		// Ignore touch if using accelerometer controls
		if(gameCfg.ctrlInput == CtrlInput.ACCELEROMETER)
			return false;

		switch(evt.getAction()) {
		case MotionEvent.ACTION_DOWN:
			pDown.x = (int)evt.getX();
			pDown.y = (int)evt.getY();
			if(gameCfg.ctrlInput == CtrlInput.TOUCH)
				setDirTouch();
			break;
		case MotionEvent.ACTION_UP:
			pUp.x = (int)evt.getX();
			pUp.y = (int)evt.getY();
			if(gameCfg.ctrlInput == CtrlInput.SWIPE)
				setDirSwipe();
			break;
		}
		return true;
	}

	protected void setDirAccel(float x, float y, float z) {
		Direction dir;
		if(gameCfg.ctrlOption == CtrlOption.RELATIVE)
			dir = setDirAccelAxis(x, DirAxis.HORIZONTAL);
		else {
			if(Math.abs(x) >= Math.abs(y))
				dir = setDirAccelAxis(x, DirAxis.HORIZONTAL);
			else
				dir = setDirAccelAxis(y, DirAxis.VERTICAL);
		}
		if(dir != null)
			gameCfg.humanCatInput = dir;
	}

	protected Direction setDirAccelAxis(float val, DirAxis dax) {
		Direction rc = null;
		if(Math.abs(val) >= gameCfg.ACCEL_THRESHOLD) {
			if(val >= gameCfg.ACCEL_THRESHOLD)
				rc = (dax == DirAxis.HORIZONTAL ? Direction.W : Direction.S);
			else
				rc = (dax == DirAxis.HORIZONTAL ? Direction.E : Direction.N);
		}
		return rc;
	}

	protected void setDirSwipe() {
		int dx, dy;
		dx = pUp.x - pDown.x;
		// If controls set to relative, we only care about left & right
		if(gameCfg.ctrlOption == CtrlOption.RELATIVE)
			gameCfg.humanCatInput = (dx >= 0 ? Direction.E : Direction.W);
		else {
			// Move in whatever direction (x or y) the swipe is longest 
			dy = pUp.y - pDown.y;
			if(Math.abs(dx) >= Math.abs(dy))
				gameCfg.humanCatInput = (dx > 0 ? Direction.E : Direction.W);
			else
				gameCfg.humanCatInput = (dy > 0 ? Direction.S : Direction.N);
		}
	}

	protected void setDirTouch() {
		if(gameCfg.ctrlOption == CtrlOption.RELATIVE) {
			// Touch left side = turn left, vice versa
			gameCfg.humanCatInput = (pDown.x < gameCfg.WIDTH_HALF ? Direction.W : Direction.E);
		}
		else {
			// Split the screen by the diagonals into 4 triangular areas.
			// A touch in a given triangle moves in that direction.
			int	x, y, yInv;
			x = pDown.x;
			// Scale Y to X range to simplify the comparisons below 
			y = (int)(pDown.y * gameCfg.ASPECT_RATIO);
			yInv = (int)((gameCfg.HEIGHT - pDown.y) * gameCfg.ASPECT_RATIO);
			if(x >= y)
				gameCfg.humanCatInput = (x >= yInv ? Direction.E : Direction.N);
			else
				gameCfg.humanCatInput = (x >= yInv ? Direction.S : Direction.W);
		}
	}
}