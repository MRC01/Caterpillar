/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/* This is the pop-up dialog where the user enters his name for a high score.
 * It is not a full screen window, but a small dialog.
*/
public class HighScoreName extends Activity {
	public static GameConfig	gameCfg	= CaterpillarMain.gameCfg;
	EditText	etNameField;
	int			hsIdx;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.scorenamedialog);
		etNameField = (EditText)findViewById(R.id.namefield);
		hsIdx = getIntent().getExtras().getInt("index");
		if(hsIdx == 0)
			setTitle("Top Score!");
		else
			setTitle("High Score # " + (hsIdx+1));
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
	}

	// get the editText value and set it in the high scores
	public void butDone(View paramView) {
		String	userName;
		
		userName = etNameField.getText().toString();
		gameCfg.highScores.get(hsIdx).sName = userName;
		// dismiss this dialog
		finish();
	}
}
