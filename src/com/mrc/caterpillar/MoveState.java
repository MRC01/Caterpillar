/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

public class MoveState {
	static final Object		CLEAROBJ = null,
							DEADOBJ = new Object();
	static final MoveState	CLEAR, DEAD;
	static {
		CLEAR = new MoveState(CLEAROBJ);
		DEAD = new MoveState(DEADOBJ);
	}
	protected Object	hit = null;

	public MoveState(Object ms) {
		hit = ms;
	}

	public boolean isDead() {
		return ((this == DEAD) || hitCat());
	}
	
	public boolean hitCat() {
		return (hit instanceof Cat);
	}

	public Cat getCat() {
		return (Cat)hit;
	}
	
	public boolean hitTarget() {
		return (hit instanceof Target);
	}

	public Target getTarget() {
		return (Target)hit;
	}
	
	public void setClear() {
		hit = CLEAR;
	}
	
	public void setDead() {
		hit = DEAD;
	}
	
	public void setCat(Cat cat) {
		hit = cat;
	}
	
	public void setTarget(Target t) {
		hit = t;
	}
}
