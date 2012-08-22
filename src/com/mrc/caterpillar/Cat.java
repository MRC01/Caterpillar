/* Copyright 1993, 2012 by Michael R. Clements
 * This software is open source and free to distribute and create derivative works.
 * But this notice of copyright and author must be retained.
*/
package com.mrc.caterpillar;

import java.util.*;
import java.io.*;

import android.graphics.*;
import android.content.res.*;

/* A cat (caterpillar) is a circular linked list of nodes, each
 * representing a segment in the cat's body.
 * The pointers go from tail to head, and Head.next is the tail.
 * No matter how long a caterpillar is, a move affects only the head & tail.
 * The move function works by changing a couple of references.
 * The grow function adds a new head node and updates a couple of pointers.
 * It all runs in constant time, regardless of the length of the cat.
 */
public class Cat {
	// cat static resources (images, sounds)
	public static class Resource {
		public Bitmap[]	headImg, segImg;
		public Sound	soundEat, soundGrow;
	}

	static Resource[]	ourResources;
	static Random		ourRand;

	// WARNING: you must call this before you instantiate the class
	public static void initResources(AssetManager am)
			throws IOException {
		if(ourResources == null) {
			ourRand = new Random();
			ourResources = new Resource[4];
			for(int i = 0; i < 4; i++) {
				Bitmap	bm;
				ourResources[i] = new Resource();
				ourResources[i].headImg = new Bitmap[4];
				ourResources[i].segImg = new Bitmap[4];
				for(int j = 0; j < 4; j++) {
					bm = Util.getBitmap("head" + i + "-" + j + ".png", am);
					ourResources[i].headImg[j] = bm;
					bm = Util.getBitmap("seg" + i + "-" + j + ".png", am);
					ourResources[i].segImg[j] = bm;
				}
				ourResources[i].soundEat = new Sound("eatApple" + i + ".mp3",
						1.0F, am);
				ourResources[i].soundGrow = new Sound("grow" + i + ".mp3",
						1.0F, am);
			}
		}
	}

	public static void adjustResources() {
		if(ourResources != null) {
			for(int j = 0; j < 4; j++) {
				for(int i = 0; i < 4; i++) {
					ourResources[j].headImg[i] = Util.resizeBitmap(
							ourResources[j].headImg[i],
							CaterpillarMain.gameCfg.SEG_SIZE);
					ourResources[j].segImg[i] = Util.resizeBitmap(
							ourResources[j].segImg[i],
							CaterpillarMain.gameCfg.SEG_SIZE);
				}
			}
		}
	}

	public static void freeResources() {
		if(ourResources != null) {
			ourRand = null;
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					ourResources[i].headImg[j].recycle();
					ourResources[i].segImg[j].recycle();
				}
				ourResources[i].soundEat.close();
				ourResources[i].soundGrow.close();
			}
			ourResources = null;
		}
	}

	// this cat's configuration
	public static class Config {
		static final int	SEG_COUNT_DEFAULT	= 5;
		public boolean		selfMove;
		public int			id, thinkAhead, startLength;
		public Direction	startDir;

		public Config(int _id, Direction _startDir, int _startLength, int _thinkAhead, boolean _selfMove) {
			id = _id;
			startDir = _startDir;
			startLength = _startLength;
			thinkAhead = _thinkAhead;
			selfMove = _selfMove;
		}
	}

	// Any node in the cat: body, head or tail (head.next is tail)
	protected class Node {
		public Node() {
			init(0, 0, Direction.N);
		}

		public Node(int x, int y, Direction dir) {
			init(x, y, dir);
		}

		protected void init(int x, int y, Direction d) {
			pos = PointFactory.self.get(x, y);
			dir = d;
		}

		Point		pos;
		Node		next;
		Direction	dir;
	}

	// Stack used to keep track of computer thinkahead moves
	Point[]				posStack;
	// Size in pixels of each segment
	protected Node		head;
	protected Direction	dir;
	protected Point		vel;
	protected int		length, growing, score;
	protected boolean	isDead;
	public Config		cfg;
	public GameConfig	gameCfg;
	public MoveState	moveStateHit;

	public Cat(Config _cfg) {
		init(_cfg);
	}

	public void init(Config _cfg) {
		// get the global game config
		gameCfg = CaterpillarMain.gameCfg;
		// initialize the position stack
		posStack = new Point[GameConfig.MAXTHINK + 1];
		cfg = _cfg;
		score = 0;
		isDead = false;
		// For efficiency, store my own MoveState hit.
		// Otherwise it gets created a lot especially during thinkahead moves. 
		moveStateHit = new MoveState(this);
		// Create the cat
		length = 1;
		dir = cfg.startDir;
		growing = cfg.startLength;
		int yMid = (gameCfg.YCELLS / 2) * gameCfg.SEG_SIZE + gameCfg.YOFFSET,
			xMid = (gameCfg.XCELLS / 2) * gameCfg.SEG_SIZE + gameCfg.XOFFSET;
		switch(dir) {
		case N:
			head = new Node(xMid + gameCfg.SEG_SIZE,
					(gameCfg.YCELLS - 1) * gameCfg.SEG_SIZE + gameCfg.YOFFSET, Direction.N);
			break;
		case S:
			head = new Node(xMid - gameCfg.SEG_SIZE, gameCfg.YOFFSET, Direction.S);
			break;
		case E:
			head = new Node(gameCfg.XOFFSET, yMid - gameCfg.SEG_SIZE, Direction.E);
			break;
		case W:
			head = new Node((gameCfg.XCELLS - 1) * gameCfg.SEG_SIZE + gameCfg.XOFFSET,
					yMid + gameCfg.SEG_SIZE, Direction.W);
			break;
		}
		head.next = head;
		turn(dir);
		while(growing > 0)
			grow();
	}

	// Draw the entire cat (used when entire screen is refreshed)
	public void draw(Canvas cvs) {
		Node seg = head.next;
		while(seg != head) {
			drawSeg(cvs, seg);
			seg = seg.next;
		}
		drawHead(cvs);
	}

	// Perform a full move (or grow), check collisions, return status
	public MoveState fullMove() {
		MoveState rc = MoveState.CLEAR;
		if(cfg.selfMove) {
			// computer driven snake; pick a move & turn
			turn(pickMove());
		}
		else if(gameCfg.humanCatInput != null) {
			// human driven snake; interpret user input
			if(gameCfg.ctrlOption == CtrlOption.RELATIVE) {
				if(gameCfg.humanCatInput == Direction.E)
					turnRight();
				else
					turnLeft();
			}
			else {
				// be nice and don't let the user go back on himself by accident
				if(gameCfg.humanCatInput.relativeDir(2) != dir)
					turn(gameCfg.humanCatInput);
			}
			// we just set this direction; set it to null so we don't set it again
			gameCfg.humanCatInput = null;
		}
		if(growing > 0) {
			ourResources[cfg.id].soundGrow.play();
			rc = grow();
		}
		else
			rc = move();
		if(rc == MoveState.CLEAR) {
			// The caterpillar hit nothing - the path is clear
			score += length;
		}
		else if(rc == MoveState.DEAD) {
			// The caterpillar hit a wall ...
			// or something that is not another caterpillar or a target.
			isDead = true;
		}
		else if(rc.hitCat()) {
			// The caterpillar hit another caterpillar (or itself)
			isDead = true;
		}
		else if(rc.hitTarget()) {
			rc.getTarget().eat(this);
		}
		return rc;
	}

	/*
	 * Moves the cat in its current direction. The old tail becomes the new head.
	 * Returns: CLEAR, TARGET, or DEAD
	 */
	public MoveState move() {
		MoveState rc;

		rc = collide();
		// put the new head's coordinates into the tail
		head.next.pos = PointFactory.self.getSum(head.pos, vel);
		head.next.dir = dir;
		// make head point to what was the tail and is now the head
		head = head.next;
		return rc;
	}

	/*
	 * Similar to move, but grows the cat 1 segment bigger.
	 * Returns: CLEAR, TARGET, or DEAD
	 */
	public MoveState grow() {
		Node newSeg;
		MoveState rc;

		rc = collide();
		// create a new head
		newSeg = new Node(head.pos.x + vel.x, head.pos.y + vel.y, dir);
		newSeg.next = head.next;
		// label the new node as the head
		head = head.next = newSeg;
		// increase length and reduce growth
		length++;
		growing--;
		return rc;
	}

	/*
	 * Adjusts the "vel" Point based on the direction parameter.
	 * N - north (up), E - east (right), S - south (down), W - west (left)
	 */
	public void turnLeft() {
		turn(dir.relativeDir(3));
	}

	public void turnRight() {
		turn(dir.relativeDir(1));
	}

	public void turn(Direction newDir) {
		dir = newDir;
		vel = PointFactory.self.getVel(dir);
	}

	/*
	 * Checks the screen position in front of the head of the cat.
	 * Returns: CLEAR, TARGET, or DEAD
	 */
	public MoveState collide() {
		return collide(getNextPos());
	}

	public MoveState collide(Point pNext) {
		MoveState	catHit;
		Target		targ;

		targ = TargetFactory.hit(pNext);
		if(targ != null)
			return targ.moveStateHit;

		// See if the cat went off screen
		if(pNext.x < 0 || pNext.x > gameCfg.XMAXPOS
				|| pNext.y < 0 || pNext.y > gameCfg.YMAXPOS)
			return MoveState.DEAD;

		// See if I hit a cat
		catHit = checkHitcats(pNext);
		if(catHit.hitCat())
			return catHit;

		return MoveState.CLEAR;
	}

	// checks whether the next move hits any cat
	public static MoveState checkHitcats(Point pNext) {
		MoveState	rc;
		
		for(int i = 0; i < CaterpillarMain.gameCfg.MAX_CATS; i++) {
			rc = checkHitcat(pNext, i);
			if(rc.isDead())
				return rc;
		}
		return MoveState.CLEAR;
	}

	// checks whether the next move hits the given cat
	protected static MoveState checkHitcat(Point pNext, int catID) {
		Node	seg;
		Cat		cat;
		cat = GameBoard.self.cats[catID];
		if(cat == null)
			return MoveState.CLEAR;

		if(pNext == cat.head.pos)
			return cat.moveStateHit;

		seg = cat.head.next;
		while(seg != cat.head) {
			if(pNext == seg.pos)
				return cat.moveStateHit;

			seg = seg.next;
		}
		return MoveState.CLEAR;
	}

	// returns the cat's next position
	protected Point getNextPos() {
		return PointFactory.self.getSum(head.pos, vel);
	}

	// Draws a cat segment on the screen.
	protected void drawSeg(Canvas cvs, Node seg) {
		cvs.drawBitmap(ourResources[cfg.id].segImg[seg.dir.ordinal()], seg.pos.x, seg.pos.y, null);
	}

	// Draws a cat head on the screen.
	protected void drawHead(Canvas cvs) {
		cvs.drawBitmap(ourResources[cfg.id].headImg[dir.ordinal()], head.pos.x, head.pos.y, null);
	}

	// Picks dir1 or dir2 randomly with equal probability
	protected Direction randomDir(Direction dir1, Direction dir2) {
		return (ourRand.nextFloat() < 0.5 ? dir1 : dir2);
	}

	/*
	 * Picks the "best" move direction for the computer-driven cat. Returns the
	 * direction (N, E, S, W).
	 */
	public Direction pickMove() {
		int			path1, path2, path3,
					dx, dy;
		Direction	choice1, choice2, altDir, back;
		Target		t;

		back = dir.relativeDir(2);
		t = TargetFactory.closest(head.pos);
		if(t != null) {
			// There is a target - try to move toward it
			dx = t.pos.x - head.pos.x;
			dy = t.pos.y - head.pos.y;
			if(dx == 0) {
				// target is directly N or S
				choice1 = (dy > 0 ? Direction.S : Direction.N);
				choice2 = randomDir(Direction.E, Direction.W);
			}
			else if(dy == 0) {
				// target is directly E or W
				choice1 = (dx > 0 ? Direction.E : Direction.W);
				choice2 = randomDir(Direction.N, Direction.S);
			}
			else {
				// target is diagonal; randomly pick a direction to step
				if(ourRand.nextFloat() < 0.5) {
					choice1 = (dy > 0 ? Direction.S : Direction.N);
					choice2 = (dx > 0 ? Direction.E : Direction.W);
				}
				else {
					choice1 = (dx > 0 ? Direction.E : Direction.W);
					choice2 = (dy > 0 ? Direction.S : Direction.N);
				}
			}
			// At this point, choice1 & choice2 must be different
			// thus they can't both be back
			if(choice1 == back) {
				choice1 = choice2;
				choice2 = dir;
			}
			else if(choice2 == back) {
				if(choice1 != dir)
					choice2 = dir;
				else
					choice2 = randomDir(dir.relativeDir(1), dir.relativeDir(3));
			}
			// AT THIS POINT, "choice1" AND "choice2" ARE BOTH:
			// A) DIFFERENT B) NEITHER POINTS "back"
			path1 = isBlocked(head.pos, dir, choice1, cfg.thinkAhead);
			if(path1 == 0)
				return choice1;
			path2 = isBlocked(head.pos, dir, choice2, cfg.thinkAhead);
			if(path2 == 0)
				return choice2;
			// THERE'S ONLY ONE MORE DIRECTION THAT COULD BE CLEAR:
			// FIND IT AND CHECK IT
			if((dir != choice1) && (dir != choice2))
				altDir = dir;
			else {
				altDir = dir.relativeDir(1);
				if((altDir == choice1) || (altDir == choice2))
					altDir = dir.relativeDir(3);
			}
			path3 = isBlocked(head.pos, dir, altDir, cfg.thinkAhead);
			if(path3 == 0)
				return altDir;
		}
		else {
			// No target - just pick a direction that is clear
			// first choice: straight ahead?
			path1 = isBlocked(head.pos, dir, dir, cfg.thinkAhead);
			if(path1 == 0)
				return dir;
			// left?
			choice2 = dir.relativeDir(3);
			path2 = isBlocked(head.pos, dir, choice2, cfg.thinkAhead);
			if(path2 == 0)
				return choice2;
			// right?
			altDir = dir.relativeDir(1);
			path3 = isBlocked(head.pos, dir, altDir, cfg.thinkAhead);
			if(path3 == 0)
				return altDir;
			// No direction is completely clear
			choice1 = dir;
		}
		// ALL DIRECTIONS ARE BLOCKED -- RETURN THE ONE THAT GOES THE FARTHEST
		if(path1 > path2) {
			if(path1 > path3)
				return choice1;
			return altDir;
		}
		else {
			if(path2 > path3)
				return choice2;
			return altDir;
		}
	}

	/*
	 * Returns 0 if "thinkAhead" moves can be made, starting in direction
	 * "dirNew" without hitting anything. Otherwise, returns the maximum # of
	 * moves that can be made starting in direction "dirNew" without hitting
	 * anything. This is a recursive function, and tests every possible sequence
	 * of moves in the specified direction. It returns as soon as it finds a way
	 * to make "thinkAhead" moves without hitting anything.
	 * 
	 * Limitations: Assumes the other cats do not move. This algorithm analyzes the
	 * board statically. The only moves that are accounted for, are the squares
	 * occupied by this cat's own prior thinkahead moves. Furthermore, even with
	 * this cat's own thinkahead moves, only its advancing head is considered;
	 * its own advancing tail is disregarded. Since the head covers previously
	 * open spaces and the tail opens previously covered spaces, This
	 * artificially constrains the choices. This can make the returned move
	 * non-ideal under several conditions.
	 * 
	 * THIS IS A DECEPTIVELY SIMPLE-LOOKING FUNCTION !!!
	 */
	protected int isBlocked(Point posBase, Direction dirBase, Direction dirNew, int level) {
		Point		vecNew, posTest;
		MoveState	ms;
		int			path1, path2, path3;

		vecNew = PointFactory.self.getVel(dirNew);
		// IF THE POINT IN QUESTION IS BLOCKED, RETURN IMMEDIATELY
		posTest = PointFactory.self.getSum(posBase, vecNew);
		ms = collide(posTest);
		if(ms.isDead())
			return cfg.thinkAhead - level + 1;

		// IF WE'RE AT THE LAST LEVEL OF RECURSION, RETURN 'CLEAR'
		if(level <= 0)
			return 0;

		// MOVE INTO THE POSITION IN QUESTION
		dirBase = dirNew;
		// CHECK PREVIOUS POSITIONS OCCUPIED BY "THINK AHEAD" MOVES
		// NOTE: it takes at least 3 moves to bump into self.
		if(level <= cfg.thinkAhead - 3) {
			for(int i = level + 3; i <= cfg.thinkAhead; i++) {
				if(posTest == posStack[i])
					return cfg.thinkAhead - level + 1;
			}
		}
		// ADD OUR POSITION TO THE "THINK AHEAD" MOVE STACK
		posStack[level] = posTest;
		// RECURSIVELY BRANCH IN EACH OF THE THREE VALID DIRECTIONS,
		// KEEPING TRACK OF HOW LONG EACH BRANCH GOES
		if((path1 = isBlocked(posTest, dirBase, dirBase, level - 1)) == 0)
			return 0;
		if((path2 = isBlocked(posTest, dirBase, dirBase.relativeDir(3), level - 1)) == 0)
			return 0;
		if((path3 = isBlocked(posTest, dirBase, dirBase.relativeDir(1), level - 1)) == 0)
			return 0;

		// ALL THE BRANCHES CRASHED -- RETURN THE LONGEST OF THE THREE
		if(path1 > path2) {
			if(path1 > path3)
				return path1;
			return path3;
		}
		else {
			if(path2 > path3)
				return path2;
			return path3;
		}
	}
}
