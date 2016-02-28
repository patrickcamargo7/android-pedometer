/*
 * Copyright Kirill Morozov 2012
 * 
 * 
	This file is part of Mapper.

    Mapper is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Mapper is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Mapper.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */

package ca.uwaterloo.mapper;

import android.graphics.PointF;
import android.hardware.Camera.Area;
import android.util.FloatMath;


/**
 * A class that represents a line segment.
 * @author Kirill
 *
 */
public class LineSegment 
{

	public final PointF start, end;
	public final float m, b; // as in, y = mx + b.
	
	public LineSegment(PointF start, PointF end){
		this.start = start;
		this.end = end;
		
		m = (end.y - start.y) / (end.x - start.x);
		b = start.y - (start.x * m);
	}
	
	private boolean isPointInSegment(float x, float y)
	{
		return ( (x >= Math.min(start.x, end.x) && x <= Math.max(start.x, end.x)) || FloatHelper.isZero(x - start.x) || FloatHelper.isZero(x - end.x)) && 
				( (y >= Math.min(start.y, end.y) && y <= Math.max(start.y, end.y)) || FloatHelper.isZero(y - start.y) || FloatHelper.isZero(y - end.y));
	}
	
	/**
	 * Given another line segment, find where they intersect.
	 * @param other the other line segment.
	 * @return the point where the two segments intersect, or null if they do not.
	 */
	public PointF findIntercept(LineSegment other)
	{
		// Special case for vertical lines
		if(Float.isInfinite(m) && Float.isInfinite(other.m)){
			if(start.x == other.start.x)
				return new PointF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
			return null;
		}
		// special case for horizontal lines
		if(m == 0 && other.m == 0){
			if(start.y == other.start.y)
				return new PointF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
			return null;
		}
		
		float x, y;
		
		if(Float.isInfinite(other.m)){
			x = other.start.x;
		}else if(Float.isInfinite(m)){
			return other.findIntercept(this);
		}else{
			// first find intercept. 
			// solve for X
			// m1x + b1 = m2x + b2
			// b1 - b2 = (m2 - m1)x
			
			x = (b - other.b) / (other.m - m);
		}
		
		// Now solve for y
		y = m*x + b;
		
		// now see if this point is on both line segments.
		if( isPointInSegment(x, y) && other.isPointInSegment(x, y)){
				//y >= Math.min(start.y, end.y) && y <= Math.max(start.y, end.y) &&
				//x >= Math.min(other.start.x, other.end.x) && x <= Math.max(other.start.x, other.end.x) &&
				//y >= Math.min(other.start.y, other.end.y) && y <= Math.max(other.start.y, other.end.y) ){
			return new PointF(x,y);
		}
		
		return null;
	}
	
	/**
	 * Returns true if point is inside this line segment.
	 * @param point
	 * @return
	 */
	public boolean containsPoint(PointF point)
	{
		if(! FloatHelper.areEqual(point.x * m + b, point.y))
			return false;
		// we know the point is on the line, if it is in the square denoted by the start and end point, then it is in the segment
		
		return point.x < Math.max(start.x, end.x) && point.x > Math.min(start.x, end.x) &&
				point.y < Math.max(start.y, end.y) && point.y > Math.min(start.y, end.y) 
				|| (
						(FloatHelper.areEqual(point.x, start.x) || FloatHelper.areEqual(point.x, end.x)) &&
						(FloatHelper.areEqual(point.y, start.y) || FloatHelper.areEqual(point.y, end.y))
					);
	}
	
	/**
	 * Calculates a unit vector that is parallel to this line segment, with the same direction
	 * @return
	 */
	public float[] findUnitVector()
	{
		float[] ret = new float[2];
		// first, calculate a vector for myself
		ret[0] = end.x - start.x;
		ret[1] = end.y - start.y;
			
		FloatHelper.convertToUnitVector(ret, ret);
		
		return ret;
	}
	
	/**
	 * This is semantically different from equality. It ignores direction. This is why I did not override .equals()
	 * @param other the other line segment.
	 * @return
	 * True if this and other are the same. False otherwise.
	 */
	public boolean theSame(LineSegment other )
	{
		if(!(other instanceof LineSegment))
			return false;
		
		if(!(
				FloatHelper.areEqual(start, other.start) && FloatHelper.areEqual(end, other.end) ||
				FloatHelper.areEqual(start, other.end) && FloatHelper.areEqual(end, other.start)
			)){
			return false;
		}
		
		if(!FloatHelper.areEqual(m, other.m) || !FloatHelper.areEqual(b, other.b)){
			return false;
		}
			
		return true;
	}
	
	/**
	 * Weaker constraint than theSame(), returns true if the line segments overlap
	 * @param other
	 * @return
	 * True if this and other are overlapping. False otherwise.
	 */
	public boolean isOverlaping(LineSegment other){
		if(!FloatHelper.areEqual(m, other.m) && !FloatHelper.areEqual(b, other.b))
			return false;
		
		// same line, now check for overlap
		// The start point of one, should be inside the other
		return containsPoint(other.start) || other.containsPoint(start);
	}
	
	/**
	 * Calculates the length of this line segment.
	 * @return the length of this line segment, in meters.
	 */
	public float length()
	{
		return FloatHelper.distance(start, end);
	}
	
}





