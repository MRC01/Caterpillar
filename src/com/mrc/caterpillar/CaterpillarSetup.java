/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/* This is the game setup screen.
 * On start, it sets the GUI to match the current GameConfig settings.
 * On exit, it sets the GameConfig settings from the GUI.
*/
public class CaterpillarSetup extends Activity {
	SeekBar		catLen;
	Spinner[]	catPick	= new Spinner[4];
	SeekBar		catThink;
	Spinner		ctrlInput;
	Spinner		ctrlOption;
	GameConfig	gameCfg;
	SeekBar		gameSpeed;

	public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(
    			WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
		gameCfg = CaterpillarMain.gameCfg;
		setContentView(R.layout.setuplayout);
		catPick[0] = ((Spinner)findViewById(R.id.catType1));
		catPick[0].setSelection(CatOption.HUMAN.ordinal());
		catPick[1] = ((Spinner)findViewById(R.id.catType2));
		catPick[1].setSelection(CatOption.NONE.ordinal());
		catPick[2] = ((Spinner)findViewById(R.id.catType3));
		catPick[2].setSelection(CatOption.COMPUTER.ordinal());
		catPick[3] = ((Spinner)findViewById(R.id.catType4));
		catPick[3].setSelection(CatOption.NONE.ordinal());
		ctrlOption = ((Spinner)findViewById(R.id.ctrlOption));
		ctrlInput = ((Spinner)findViewById(R.id.ctrlInput));
		catLen = ((SeekBar)findViewById(R.id.catLen));
		catThink = ((SeekBar)findViewById(R.id.catThink));
		gameSpeed = ((SeekBar)findViewById(R.id.gameSpeed));
	}

	protected void onResume() {
		super.onResume();
		toGui();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void toGui() {
		for(int i = 0; i < catPick.length; i++) {
			if(gameCfg.catCfg[i] == null)
				continue;
			catPick[i].setSelection(gameCfg.catCfg[i].ordinal());
		}
		if(gameCfg.ctrlOption != null)
			ctrlOption.setSelection(gameCfg.ctrlOption.ordinal());
		if(gameCfg.ctrlInput != null)
			ctrlInput.setSelection(gameCfg.ctrlInput.ordinal());
		catLen.setProgress(gameCfg.catLen);
		catThink.setProgress(gameCfg.catThink);
		gameSpeed.setProgress(gameCfg.gameSpeed);
	}

	protected void fromGui() {
		for(int j = 0; j < catPick.length; j++) {
			int i = catPick[j].getSelectedItemPosition();
			gameCfg.catCfg[j] = CatOption.values()[i];
		}
		gameCfg.ctrlOption = CtrlOption.values()[ctrlOption.getSelectedItemPosition()];
		gameCfg.ctrlInput = CtrlInput.values()[ctrlInput.getSelectedItemPosition()];
		gameCfg.catLen = catLen.getProgress();
		if(gameCfg.catLen >= gameCfg.MAXLEN)
			gameCfg.catLen = gameCfg.MAXLEN;
		gameCfg.catThink = catThink.getProgress();
		if(gameCfg.catThink >= gameCfg.MAXTHINK)
			gameCfg.catThink = gameCfg.MAXTHINK;
		gameCfg.gameSpeed = gameSpeed.getProgress();
		if(gameCfg.gameSpeed >= gameCfg.MAXSPEED)
			gameCfg.gameSpeed = gameCfg.MAXSPEED;
	}

	public void butReturn(View paramView) {
		fromGui();
		finish();
	}
}
