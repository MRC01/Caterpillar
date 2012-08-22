/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import android.graphics.*;

/* This class represents any target that the caterpillars can eat and manages all targets.
 * Each particular target is a subclass.
*/
public abstract class Target {
	static GameConfig	gameCfg;
	int			lifespanBase,
				lifespan;
	Point		pos;
	boolean		alive;
	int			points;
	Bitmap		img;
	MoveState	moveStateHit;

	public Target(Point _pos, int maxPoints) {
		init(_pos, maxPoints);
	}

	public void init(Point _pos, int maxPoints) {
		gameCfg = TargetFactory.getGameCfg();
		alive = true;
		pos = _pos;
		points = (int)(Math.random() * maxPoints);
		countInc();
		TargetFactory.add(this);
		moveStateHit = new MoveState(this);
	}

	// ages the target (randomly drops the score) - returns FALSE if dead
	public boolean stillAlive() {
		if(alive && shouldAge()) {
			points--;
			if(points <= 0)
				die();
		}
		return alive;
	}

	// subclasses override this to do whatever happens when the cat eats it
	protected abstract void subEat(Cat cat);

	// called whenever the given cat eats this target
	public void eat(Cat cat) {
		subEat(cat);
		die();
	}

	public void die() {
		alive = false;
		countDec();
		TargetFactory.remove(this);
	}

	public void draw(Canvas cvs) {
		cvs.drawBitmap(img, pos.x, pos.y, null);
	}

	// returns TRUE if the target should age (reduce score)
	protected boolean shouldAge() {
		return((int)(Math.random() * getLifespan()) == 0);
	}

	// target lifespan is proportional to the area of the board
	// (this makes probability of reaching it roughly constant independent of
	// board size)
	protected int getLifespan() {
		if(lifespan != 0)
			return lifespan;
		lifespan = (int)(((double)lifespanBase / 1000.0) * gameCfg.XCELLS * gameCfg.YCELLS + 0.5);
		return lifespan;
	}

	// subclasses override these methods count the number of instances currently alive
	protected abstract void countInc();
	protected abstract void countDec();
}
