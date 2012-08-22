/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.io.*;
import java.util.*;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/* This is the high scores screen.
 * We don't actually save or load high scores here...
 * We define those functions but they're called by the main screen.
 */
public class CaterpillarScore extends Activity {
	GameConfig	gameCfg;

	public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(
    			WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
		this.gameCfg = CaterpillarMain.gameCfg;
		setContentView(R.layout.scorelayout);
	}

	// If the given score is in the list, adds it.
	// Returns index of score in list, -1 if not on the list
	public static int checkHighScore(int newScore) {
		GregorianCalendar cal;
		GameConfig gameCfg;
		int rc = -1;

		gameCfg = CaterpillarMain.gameCfg;
		for(int i = 0; i < gameCfg.MAX_SCORES; i++) {
			if(newScore > gameCfg.highScores.get(i).sScore) {
				cal = new GregorianCalendar();
				cal.setTime(new Date());
				GameConfig.HighScore hs = gameCfg.new HighScore();
				hs.sScore = newScore;
				hs.sName = "Computer";
				hs.sDate = (cal.get(Calendar.MONTH) + 1) + "/"
						+ cal.get(Calendar.DAY_OF_MONTH) + "/"
						+ cal.get(Calendar.YEAR);
				gameCfg.highScores.add(i, hs);
				gameCfg.highScores.remove(gameCfg.MAX_SCORES);
				rc = i;
				break;
			}
		}
		return rc;
	}

	public static void loadScores() throws IOException {
		BufferedReader buf;
		GameConfig gameCfg = CaterpillarMain.gameCfg;
		int		i = 0, j;

		gameCfg.highScores = new ArrayList<GameConfig.HighScore>();
		// read scores from the file (if possible)
		try {
			buf = Util.readFile(gameCfg.HIGHSCORE_FILE);
			if(buf != null) {
				for(i = 0; buf.ready() && i < gameCfg.MAX_SCORES; i++) {
					GameConfig.HighScore hs = gameCfg.new HighScore();
					String str = buf.readLine();
					if(str != null)
						hs.sScore = Integer.parseInt(str);
					str = buf.readLine();
					if(str != null)
						hs.sName = str;
					str = buf.readLine();
					if(str != null)
						hs.sDate = str;
					gameCfg.highScores.add(hs);
				}
			}
		}
		catch(Exception e) {
			// If an exception occurs we'll do the right logic below
		}
		// if we didn't read enough scores, create empties for the rest
		for(j = i; j < gameCfg.MAX_SCORES; j++) {
			gameCfg.highScores.add(gameCfg.new HighScore());
		}
		Collections.sort(gameCfg.highScores);
		Collections.reverse(gameCfg.highScores);
	}

	public static void writeScores() throws IOException {
		GameConfig gameCfg = CaterpillarMain.gameCfg;
		if(gameCfg.highScores != null) {
			BufferedWriter buf = Util.writeFile(gameCfg.HIGHSCORE_FILE);
			if(buf != null) {
				for(int i = 0; i < gameCfg.highScores.size(); i++) {
					GameConfig.HighScore hs = gameCfg.highScores.get(i);
					buf.write(Integer.toString(hs.sScore));
					buf.newLine();
					buf.write(hs.sName);
					buf.newLine();
					buf.write(hs.sDate);
					buf.newLine();
				}
				buf.close();
			}
		}
	}

	public void butReturn(View paramView) {
		finish();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
		setScoreFields(0, R.id.score1score, R.id.score1name, R.id.score1date);
		setScoreFields(1, R.id.score2score, R.id.score2name, R.id.score2date);
		setScoreFields(2, R.id.score3score, R.id.score3name, R.id.score3date);
		setScoreFields(3, R.id.score4score, R.id.score4name, R.id.score4date);
		setScoreFields(4, R.id.score5score, R.id.score5name, R.id.score5date);
	}

	protected void setScoreFields(int rank, int fldScore, int fldName, int fldDate) {
		String	strScore = Util.stringFromInt(gameCfg.highScores.get(rank).sScore);
		((TextView)findViewById(fldScore)).setText(strScore);
		((TextView)findViewById(fldName)).setText(gameCfg.highScores.get(rank).sName);
		((TextView)findViewById(fldDate)).setText(gameCfg.highScores.get(rank).sDate);
	}
}
