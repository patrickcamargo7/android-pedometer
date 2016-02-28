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

/**
 * Contains a point and the piece of geometry it intersects.
 * @author Kirill
 *
 */
public class InterceptPoint 
{

	private LineSegment line;
	private PointF point;
	
	public InterceptPoint(LineSegment seg, PointF p){
		line = seg;
		point = p;
	}

	/**
	 * Returns the line segment this intercept point is on.
	 * @return
	 */
	public LineSegment getLine() {
		return line;
	}

	/**
	 * The point that this intercept point represents.
	 * @return
	 */
	public PointF getPoint() {
		return point;
	}
}
