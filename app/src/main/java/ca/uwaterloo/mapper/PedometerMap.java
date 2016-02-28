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

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;

/**
 * A representation of a map. All values stored in meters.
 * @author Kirill
 *
 */
public class PedometerMap 
{
	public List<ArrayList<PointF>> paths = new ArrayList<ArrayList<PointF>>();
	public PointF size = new PointF();
	
}
