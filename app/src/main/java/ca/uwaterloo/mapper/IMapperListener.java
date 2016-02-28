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
 * Implement this interface to listen to user-generated events sent by the mapper.
 * 
 * You will need to call Mapper.addListener() to get the events.
 * 
 * @author Kirill
 *
 */
public interface IMapperListener 
{
	/**
	 * The method that is called when the user sets their location through the Mapper.
	 * @param source The Mapper that caused the change.
	 * @param loc The new coordinates of the location in meters.
	 */
	public void locationChanged(Mapper source, PointF loc);
	/**
	 * The method that is called when the user sets their destination through the Mapper.
	 * @param source The Mapper that caused the change.
	 * @param dest The new coordinates of the destination in meters.
	 */
	public void DestinationChanged(Mapper source, PointF dest);
}
