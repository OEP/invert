package org.oep.invert;

public class Point {
	public float x, y;
	
	public Point () { }
	
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	private float distance(Point point) {
		return (float) Math.sqrt( (x - point.x) * (x - point.x) + (y - point.y) * (y - point.y) );
	}
	
	/**
	 * Given an origin point, return the point in absolute terms
	 * @param origin
	 * @return
	 */
	public Point absolute(Point origin) {
		return new Point(origin.x + x, origin.y + y);
	}
	
	public static Point rotateAround(Point original, float zoom, double rotateAngle) {
		return Point.rotateAround(new Point(0,0), original, zoom, rotateAngle);
	}
	
	/**
	 * Rotate a point around a given center-point by a given angle (in radians) and zoom in by a given zoom factor.
	 * @param center
	 * @param point
	 * @param zoom
	 * @param rotateAngle
	 * @return
	 */
	public static Point rotateAround(Point center, Point point, float zoom, double rotateAngle) {
		// Get the radial distance from the center of the figure
		float distance = zoom *  center.distance( point );
		
		// For ease of computation
		float dy = point.y - center.y;
		float dx = point.x - center.x;
		
		// Get this point's phase (there are special cases for x or y = 0)
		float phase = 0;
		if(dx != 0 && dy != 0) {
			phase = (float) Math.atan(dy / dx);
			
			if(phase < 0 && dx < 0) {
				phase += Math.PI;
			}
			else if(phase > 0 && dx < 0 && dy < 0) {
				phase += Math.PI;
			}
		}
		else if(dx == 0 && dy != 0)
			phase = (float) ((dy > 0) ? Math.PI / 2 : 3 * Math.PI / 2);
		else if(dy == 0 && dx != 0) //
			phase = (float) ((dx > 0) ? 0 : Math.PI); 
		
		// Get this point's true angle (in radians)
		double angle = rotateAngle + phase;
		
		// Compute the new x-y coordinates
		float newX = (float) (distance * Math.cos(angle));
		float newY = (float) (distance * Math.sin(angle));
		
		return new Point(newX, newY);
	}

}
