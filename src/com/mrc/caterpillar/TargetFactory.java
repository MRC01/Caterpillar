/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.util.*;

import android.graphics.*;

/* This class manages all targets of all types.
*/
public class TargetFactory {
	static GameConfig	gameCfg;
	static List<Target>	targets = new ArrayList<Target>();
	static int			countApple = 0,
						countLeaf = 0,
						countClock = 0;

	protected static GameConfig getGameCfg() {
		if(gameCfg == null)
			gameCfg = CaterpillarMain.gameCfg;
		return gameCfg;
	}

	public static void clear() {
		getGameCfg();
		targets.clear();
		TargetApple.countZero();
		TargetClock.countZero();
		TargetLeaf.countZero();
	}

	public static void add(Target t) {
		targets.add(t);
	}
	
	public static void remove(Target t) {
		targets.remove(t);
	}
	
	// return which target the given Point hits (null if none)
	public static Target hit(Point pos) {
		for(Target t : targets)
			if(pos.equals(t.pos))
				return t;
		return null;
	}

	// age & update all targets
	public static void update() {
		for(int i = 0; i < targets.size(); i++) {
			Target t = targets.get(i);
			// NOTE: this removes it from the list, so we decrement the list pointer
			if(!t.stillAlive()) i--;
		}
		// ensure we always have the right amount of targets of various types
		if(needNewLeaf())
			newLeaf();
		if(needNewApple())
			newApple();
		if(needNewClock())
			newClock();
	}

	public static boolean needNewApple() {
		return ((TargetApple.instanceCount < gameCfg.countApple)
				&& (Util.random(gameCfg.chanceApple) == 0));
	}
	
	public static void newApple() {
		Point tPos = newTargetPos();
		if(tPos != null) {
			// No need to save it; the constructor puts it in my static list
			new TargetApple(tPos);
		}
	}

	public static boolean needNewLeaf() {
		return (TargetLeaf.instanceCount < gameCfg.countLeaf);
	}
	
	public static void newLeaf() {
		Point tPos = newTargetPos();
		if(tPos != null) {
			// No need to save it; the constructor puts it in my static list
			new TargetLeaf(tPos);
		}
	}

	public static boolean needNewClock() {
		return ((TargetClock.instanceCount < gameCfg.countClock)
				&& (Util.random(gameCfg.chanceClock) == 0));
	}
	
	public static void newClock() {
		Point tPos = newTargetPos();
		if(tPos != null) {
			// No need to save it; the constructor puts it in my static list
			new TargetClock(tPos);
		}
	}

	protected static Point newTargetPos() {
		/* Create a new target, but do not allow it to be on an occupied space.
		 * If on an occupied space, we don't want to pick another random spot
		 * because that could lead to an infinite loop (though unlikely).
		 * Instead, just skip for now, there will be no target,
		 * we'll try again next loop iteration a few milliseconds later.
		 */
		Point	tPos = Util.randomPos();
		if((hit(tPos) == null) && (Cat.checkHitcats(tPos) == MoveState.CLEAR))
			return tPos;
		return null;
	}

	// returns the target closest to the given position (null if none)
	public static Target closest(Point pos) {
		if(targets.isEmpty())
			return null;
		return targets.get(0);
	}
	
	// draw all targets
	public static void drawAll(Canvas cvs) {
		for(Target t : targets)
			t.draw(cvs);
	}
}
