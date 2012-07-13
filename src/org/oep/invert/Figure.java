package org.oep.invert;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * A figure is mathematically a 2D indirected graph data structure, but meant for graphics and
 * animation. Think of it as a way of drawing stick figures.
 * @author pkilgo
 *
 */
public class Figure {
	// A simple point a to point b mapping
	private Vector<int[]> mEdges = new Vector<int[]>();
	
	// A mapping of animation names to animations
	private HashMap<String, FigureAnimation> mAnimations = new HashMap<String,FigureAnimation>();
	
	// The current animation that is playing
	private FigureAnimation mAnimation;
	
	// Is the current animation looping
	private boolean mLooping = true;
	
	// The interpolation speed
	private int mSpeed = 0;
	
	// The velocity
	private float [] mVelocity = new float [2];
	
	// The zoom factor
	private float mZoom = 1.5f;
	
	// The interpolation state
	private int mInterpolationState = 0;
	
	// The index of the current frame
	private int mFrameIndex = 0;
	
	// Index of the previous frame
	private int mPreviousFrameIndex = 0;
	
	// The current frame object
	private FigureFrame mCurrentFrame;
	
	// The previous frame object
	private FigureFrame mPreviousFrame;
	
	// The generated frame object (after the image is interpolated/rotated/etc
	private FigureFrame mGeneratedFrame;
	
	// The dimension (number of points) of this figure
	private int mDimension = 0;
	
	// The position of the origin of the figure
	private Point mPosition = new Point();
	
	// The orientation (in radians) of the figure
	private double mOrientation = 0;

	// The collision rectangle...
	private Rect mCollisionRect;

	private static float POINT_RADIUS = 4;
	
	public Figure(int points, int x, int y) {
		mDimension = points;
		mPosition.x = x;
		mPosition.y = y;
	}
	
	/**
	 * Try to add an animation identified by 'name'
	 * @param name
	 * @param animation
	 */
	public void addAnimation(String name, FigureAnimation animation) {
		if(animation.getDimension() == mDimension)
			mAnimations.put(name, animation);
		else
			throw new IllegalArgumentException("Animation dimension must match Figure dimension");
	}
	
	/**
	 * Adds an edge, referenced by two point IDs.
	 * @param a
	 * @param b
	 */
	public void addEdge(int a, int b) {
		if(a >= mDimension || b >= mDimension || a < 0 || b < 0)
			throw new IllegalArgumentException("Point IDs must be less then Figure dimension and >= 0");
		
		int edge[] = new int [2];
		edge[0] = a;
		edge[1] = b;
		
		mEdges.add(edge);
	}
	
	/**
	 * Try to play an animation identified by 'name'
	 * @param name
	 * @param looping
	 */
	public void playAnimation(String name, int speed, boolean looping) {
		if(mAnimations.containsKey(name) == false)
			throw new IllegalArgumentException("Figure does not contain animation '" + name + "'");
		
		if(speed < 1 || speed > 100) {
			throw new IllegalArgumentException("Speed is bounded by [1,100]");
		}
		
		mAnimation = mAnimations.get(name);
		mLooping = looping;
		mSpeed = speed;
		mFrameIndex = 0;
	}
	
	/**
	 * Update the Figure so that the next frame is ready to be displayed.
	 */
	public void nextFrame() {
		// We change the frame if the interpolation state reaches 100 this iteration
		if(mInterpolationState >= 100 - mSpeed) {
			mPreviousFrameIndex = mFrameIndex;
			mPreviousFrame = currentFrame();
			
			// Either loop back to Frame 0 or...
			if(mLooping) mFrameIndex = (mFrameIndex + 1) % mAnimation.getLength();
			// Stick to the last frame
			else mFrameIndex = Math.min(mFrameIndex + 1, mAnimation.getLength() - 1);
			
			// Go ahead and load the next frame
			mCurrentFrame = mAnimation.getFrame(mFrameIndex);
		}
		
		// Update the interpolation state
		mInterpolationState = (mInterpolationState + mSpeed) % 100;
		
		// Generate the new frame and collision box
		mGeneratedFrame = new FigureFrame();
		mCollisionRect = new Rect();
		
		Point firstPoint = Point.rotateAround(getInterpolatedPoint(0), mZoom, mOrientation);
		mCollisionRect.bottom = mCollisionRect.top = (int) firstPoint.y;
		mCollisionRect.left = mCollisionRect.right = (int) firstPoint.x;
		
		for(int i = 0; i < currentFrame().getDimension(); i++) {
			Point relPoint = Point.rotateAround(getInterpolatedPoint(i), mZoom, mOrientation);
			mGeneratedFrame.addPoint(relPoint.x, relPoint.y);
			
			mCollisionRect.bottom = (int) (Math.max(mCollisionRect.bottom, relPoint.y));
			mCollisionRect.top = (int) (Math.min(mCollisionRect.top, relPoint.y));
			
			mCollisionRect.left = (int) (Math.min(mCollisionRect.left, relPoint.x));
			mCollisionRect.right = (int) (Math.max(mCollisionRect.right, relPoint.x));
		}
		
		// Convert to absolute terms
		mCollisionRect.bottom += mPosition.y;
		mCollisionRect.top += mPosition.y;
		mCollisionRect.left += mPosition.x;
		mCollisionRect.right += mPosition.x;
	}
	
	private FigureFrame currentFrame() {
		if(mCurrentFrame == null) {
			mCurrentFrame = mAnimation.getFrame(mFrameIndex);
			return mCurrentFrame;
		}
		else
			return mCurrentFrame;
	}
	
	private FigureFrame previousFrame() {
		if(mPreviousFrame == null) {
			mPreviousFrame = mAnimation.getFrame(mPreviousFrameIndex);
			return mPreviousFrame;
		}
		else
			return mPreviousFrame;
	}
	
	/**
	 * This method draws the generated frame made by nextFrame()
	 * @param canvas
	 * @param paint
	 */
	public void paint(Canvas canvas, Paint paint) {
		if(mGeneratedFrame == null)
			throw new IllegalArgumentException("No frame has been generated");
		
		// Draw the points as little balls
		for(int i = 0; i < mGeneratedFrame.getDimension(); i++) {
			Point absPoint = mGeneratedFrame.getPoint(i).absolute(mPosition);
			
			if(i == 0)
				canvas.drawCircle(absPoint.x, absPoint.y, POINT_RADIUS, paint);
			
//			canvas.drawText(String.valueOf(i + 1), np[0], np[1] - 5, paint);
		}
		
		// Draw the edges as little lines between the points
		for(int i = 0; i < mEdges.size(); i++) {
			final int [] edge = mEdges.elementAt(i);
			
			// Get the absolute points
			Point p1 = mGeneratedFrame.getPoint(edge[0]).absolute(mPosition);
			Point p2 = mGeneratedFrame.getPoint(edge[1]).absolute(mPosition);
			
			canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
		}
	}
	
	/**
	 * This is an easy way of getting a point that has been interpolated for
	 * the currently playing animation.
	 * @param n, the index of the point
	 * @return
	 */
	private Point getInterpolatedPoint(int n) {
		if(n < 0 || n >= mAnimation.getDimension()) 
			throw new IllegalArgumentException("Invalid point ID");
		
		Point B = currentFrame().getPoint(n);
		Point A = previousFrame().getPoint(n);
		
		float x = A.x + (B.x - A.x) * mInterpolationState / 100;
		float y = A.y + (B.y - A.y) * mInterpolationState / 100;
		
		return new Point(x,y);
	}
	
	/**
	 * Rotate the figure by the number of degrees
	 * @param angle
	 */
	public void rotate(double angle) {
		// TODO: Protect from overflow
		mOrientation += angle;
	}
	
	
	/**
	 * Set the orientation of this figure to a particular angle in degrees
	 * @param angle
	 */
	public void setOrientationInDegrees(double angle) {
		mOrientation = angle * Math.PI / 180;
	}
	
	
	public void setPosition(float x, float y) {
		mPosition.x = x;
		mPosition.y = y;
	}

	public void zoom(float zoomVelocity) {
		mZoom = Math.max(0.01f, mZoom + zoomVelocity);
	}
	
	
	public void accelerate(double ax, double ay) {
		// This is inverted for some reason
		mVelocity[0] -= ax;
		mVelocity[1] += ay;
	}
	
	public void move(Rect bounds) {
		if(bounds == null) {
			mPosition.x += mVelocity[0];
			mPosition.y += mVelocity[1];
			return;
		}
		
		int minY = (int) (bounds.top + mPosition.y - mCollisionRect.top);
		int maxY = (int) (bounds.bottom - (mCollisionRect.bottom - mPosition.y));
		int minX = (int) (bounds.left + mPosition.x - mCollisionRect.left);
		int maxX = (int) (bounds.right - (mCollisionRect.right - mPosition.x));
		
		mPosition.x = Math.max(minX, Math.min(maxX, mPosition.x + mVelocity[0]));
		mPosition.y = Math.max(minY, Math.min(maxY, mPosition.y + mVelocity[1]));
		
		if(mPosition.x == maxX || mPosition.x == minX)
			mVelocity[0] = 0;
		
		if(mPosition.y == maxY || mPosition.y == minY)
			mVelocity[1] = 0;
	}

	public void setVelocity(float x, float y) {
		mVelocity[0] = x;
		mVelocity[1] = y;
	}
	
	public boolean collidesWith(Rect r) {
		if(mCollisionRect == null) return false;
		return mCollisionRect.intersect(r);
	}
	
	public boolean containedWithin(Rect r) {
		if(mCollisionRect == null || r == null) return false;
		return r.contains(mCollisionRect);
	}
	
	/*public void getInTheBox(Rect box) {
		if(mCollisionRect == null || box == null || box.contains(mCollisionRect)) return;
		

		
		mPosition.y = Math.max(minY, Math.min(maxY, mPosition.y));
	}*/
	
	public float getX() {
		return mPosition.x;
	}
	
	public float getY() {
		return mPosition.y;
	}
	
	public String toString() {
		return "Figure: (" + mPosition.x + ", " + mPosition.y + ")";
	}
}

/**
 * Helper class to store the figure's frames.
 * @author pkilgo
 *
 */
class FigureAnimation {
	private Vector<FigureFrame> mFrames = new Vector<FigureFrame>();
	private int mDimension = 0;
	
	public void addFrame(FigureFrame frame) {
		// Assume the dimension of this animation
		if(mFrames.size() == 0) mDimension = frame.getDimension();
		else if(mDimension != frame.getDimension()) {
			throw new IllegalArgumentException("Frame dimension must match Animation dimension");
		}
		
		
		mFrames.add(frame);
	}
	
	/**
	 * Returns the number of frames of this animation
	 * @return
	 */
	public int getLength() {
		return mFrames.size();
	}
	
	public FigureFrame getFrame(int n) {
		return mFrames.elementAt(n);
	}

	public int getDimension() {
		return mDimension;
	}
}

/**
 * Helper class the store the points in a frame
 * @param points
 */
class FigureFrame {
	private Vector<Point> mPoints = new Vector<Point>();
	
	public FigureFrame() { }
	
	/**
	 * Add another point to the frame.
	 * @param x
	 * @param y
	 */
	public void addPoint(float x, float y) {
		mPoints.add(new Point(x,y));
	}
	
	/**
	 * Returns the number of points in this frame.
	 * @return the number of points
	 */
	public int getDimension() {
		return mPoints.size();
	}
	
	public final Point getPoint(int n) {
		return mPoints.elementAt(n);
	}
}
