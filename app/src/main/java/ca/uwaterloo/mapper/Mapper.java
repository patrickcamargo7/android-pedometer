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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnLongClickListener;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

/**
 * Mapper class for getting user input reletive to an svg map loaded by the MapLoader
 * 
 * The Mapper's coordinates' origin is in the top left hand corner.
 * @author Kirill
 *
 */
public class Mapper extends View
{
	private float fieldWidth = 400;
	private float fieldHeight = 400;
	
	private GestureDetector gestureDetector;
	private Handler handler;
	
	private List<IMapperListener> listeners = new ArrayList<IMapperListener>();
	private List<LabeledPoint> labeledPoints = new ArrayList<LabeledPoint>();
	
	
	private PointF startPoint = new PointF(), 
			destPoint = new PointF(), 
			userPoint = new PointF(),
			selectPoint = new PointF();
	
	private List<PointF> userPath = new ArrayList<PointF>();
	
	private int SET_LOCATION_ID = 0;
	private int SET_DESTINATION_ID = 1;
	
	private List<Paint> linePaints = new ArrayList<Paint>();
	public final int[] defalutColors ={
			0xff000000, // lines
			0xffff0000, // user point
			0xffff0000, // user path
			0xff00ff00, // end
			0xffffff00, // start
			0xffff00ff, // labeled point
			};
	
	private static final int LINE_COLOR_INDEX = 0;
	private static final int USER_POINT_COLOR_INDEX = 1;
	private static final int USER_PATH_COLOR_INDEX = 2;
	private static final int END_POINT_COLOR_INDEX = 3;
	private static final int START_POINT_COLOR_INDEX = 4;
	private static final int LABELlED_POINT_COLOR_INDEX = 5;
	
	
	PedometerMap map = new PedometerMap();
	PointF scale;

	
	/**
	 * Initializes a new mapper object.
	 * @param context context The application context. You can get your application context by calling getApplicationContext() from your Activity
	 * @param sizeX The width of the mapper
	 * @param sizeY The Height of the mapper
	 * @param xScale The number of pixles to use per meter in the X axis
	 * @param yScale The number of pixles to use per meter in the Y axis
	 */
	public Mapper(Context context, float sizeX, float sizeY, float xScale, float yScale) {
		super(context);
		
		fieldWidth = sizeX;
		fieldHeight = sizeY;
		
		handler = new Handler();
		gestureDetector = new GestureDetector(context, new MapperGestureDetector(this), handler);
		
		for(int i =0; i < defalutColors.length; i++)
			linePaints.add(new Paint());
		setColors(defalutColors);
		
		scale = new PointF(xScale, yScale);
	}
	
	/**
	 * Sets the colors for the y-values of the graph. Order should match the order of the labels.
	 * 
	 * Colors are represented by an integer that looks like this:
	 * 
	 * 0xAARRGGBB
	 * 
	 * where AA = alpha;
	 *       RR = red;
	 *       GG = green;
	 *       BB = blue;
	 *       
	 * You can initialize an array of colors like this:
	 * 
	 * private int[] colors = {0xffff0000, 
	 * 						0xff00ff00,
	 * 						0xff0000ff}
	 * 
	 * The array of colors should contain them in this order:
	 * 
	 * {Lines, User point, User path, End point, Start point, Labelled points}
	 * 
	 * @param colors
	 */
	public void setColors(int[] colors)
	{
		for(int i = 0; i < Math.min(linePaints.size(), colors.length); i++)
			linePaints.get(i).setColor(colors[i]);
	}
	
	/**
	 * Adds a Listener for responding to changes in the start and end point. 
	 * @param listener
	 */
	public void addListener(IMapperListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes an existing listener.
	 * @param listener
	 */
	public void removeListener(IMapperListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Set the "user point" of the mapper. This is a special point that is drawn in a different color (red by default) 
	 * @param point The value of the point is coppied to the mapper.
	 */
	public void setUserPoint(PointF point)
	{
		userPoint.set(point);
		invalidate();
	}
	
	
	/** 
	 * Returns the last place the user point was set to.
	 * @return
	 */
	public PointF getUserPoint()
	{
		return userPoint;
	}
	
	/**
	 * Set the "user point" of the mapper. This is a special point that is drawn in a different color. (red by default) 
	 * @param x in meters
	 * @param y in meters
	 */
	public void setUserPoint(float x, float y)
	{
		userPoint.set(x, y);
		invalidate();
	}
	
	/**
	 * Set the "user path". This is a series of points that are drawn as a line of a different color. (red by default)
	 * @param points The points' location (in meters)
	 */
	public void setUserPath(List<PointF> points)
	{
		userPath.clear();
		
		if(points != null)
			userPath.addAll(points);
		
		invalidate();
	}
	
	/**
	 * Adds a labelled point. These are a series of points that are drawn with the indicated labels.
	 * @param point The point's location (in meters). Values are coppied.
	 * @param label The associated label
	 * @return
	 */
	public LabeledPoint addLabeledPoint(PointF point, String label)
	{
		LabeledPoint ret = new LabeledPoint(point, label);
		labeledPoints.add(ret);
		invalidate();
		return ret;
	}
	
	public void removeLabeledPoint(PointF point)
	{
		labeledPoints.remove(point);
		invalidate();
	}
	
	public void removeAllLabeledPoints()
	{
		labeledPoints.clear();
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		 boolean ret = super.onTouchEvent(event);
		 boolean ret2 = gestureDetector.onTouchEvent(event);
		 
		 return ret || ret2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension((int)fieldWidth, (int)fieldHeight);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		
		for(ArrayList<PointF> path : map.paths){
			for(int i = 0; i < path.size() - 1; i++){
				canvas.drawLine(path.get(i).x * scale.x, path.get(i).y * scale.y, 
								path.get(i+1).x * scale.x, path.get(i+1).y * scale.y, 
								linePaints.get(LINE_COLOR_INDEX));
			}
		}
		
		for(int i = 0; i < userPath.size() - 1; i++){
			canvas.drawLine(userPath.get(i).x * scale.x, userPath.get(i).y * scale.y, 
							userPath.get(i+1).x * scale.x, userPath.get(i+1).y * scale.y, 
							linePaints.get(USER_PATH_COLOR_INDEX));
		}
		
		for(LabeledPoint point : labeledPoints){
			canvas.drawCircle(point.point.x * scale.x, point.point.y * scale.y, 4, linePaints.get(LABELlED_POINT_COLOR_INDEX));
			canvas.drawText(point.label, 2 + point.point.x * scale.x, point.point.y * scale.y, linePaints.get(LINE_COLOR_INDEX));
		}
		
		canvas.drawCircle(startPoint.x * scale.x, startPoint.y * scale.y, 10, linePaints.get(START_POINT_COLOR_INDEX));
		canvas.drawText("Start", 5 + startPoint.x * scale.x, startPoint.y * scale.y, linePaints.get(LINE_COLOR_INDEX));
		
		canvas.drawCircle(destPoint.x * scale.x, destPoint.y * scale.y, 10, linePaints.get(END_POINT_COLOR_INDEX));
		canvas.drawText("End", 5 + destPoint.x * scale.x, destPoint.y * scale.y, linePaints.get(LINE_COLOR_INDEX));
		
		canvas.drawCircle(userPoint.x * scale.x, userPoint.y * scale.y, 5, linePaints.get(USER_POINT_COLOR_INDEX));
		canvas.drawText("User", 2.5f + userPoint.x * scale.x, userPoint.y * scale.y, linePaints.get(LINE_COLOR_INDEX));		
		
	}


	/**
	 * Sets the given PedometerMap as the map displayed by the mapper. The map file should be loaded by the MapLoader class.
	 * @param newMap
	 */
	public void setMap(PedometerMap newMap)
	{
		map = newMap;
		invalidate();
	}
	
	/**
	 * 
	 * @return The point the user has marked as the his current location through the UI.
	 */
	public PointF getStartPoint(){
		return startPoint;
		
	}
	
	/**
	 * 
	 * @return The point the user wishes ti travel to.
	 */
	public PointF getEndPoint(){
		return destPoint;
	}
	
	/**
	 * Calculates if a given line intersects some other lines of the geometry of the current map
	 * 
	 * @param start the start point of the line to calculate (in meters)
	 * @param end The end point of the line to calculate (in meters)
	 * @return A list of points where the sample line intersects with the geometry. 
	 * The intercepts are sorted by distance to the start point.
	 */
	public List<InterceptPoint> calculateIntersections(final PointF start, final PointF end)
	{
		List<InterceptPoint> ret = new ArrayList<InterceptPoint>();
		LineSegment query = new LineSegment(start, end);
		
		for(ArrayList<PointF> path : map.paths){
			for(int i = 0; i < path.size() - 1; i++){
				LineSegment segm = new LineSegment(path.get(i), path.get(i+1));
				
				if(segm.theSame(query))
					continue;
				
				PointF p = query.findIntercept(segm);
				if(p != null){
					ret.add(new InterceptPoint(segm, p));
				}
				
			}
		}
		
		Collections.sort(ret, new Comparator<InterceptPoint>(){

			public int compare(InterceptPoint arg0, InterceptPoint arg1) {
				float distStart0 = FloatHelper.distance(start, arg0.getPoint());
				float distStart1 = FloatHelper.distance(start, arg1.getPoint());
				if(FloatHelper.isZero(distStart0 - distStart1))
					return 0;
				else if(distStart0  < distStart1)
					return -1;
				else
					return 1;
			}
		});
		
		return ret;
	}

	/**
	 * 
	 * @param point The point from which to calculate geometry
	 * @return
	 *  All the geometry that starts at or passes through this point as line segments.
	 * The Start member of each line segment will satisfy: 
	 * 
	 * {@code
	 * FloatHelper.areEqual(point, start);}
	 * 
	 */
	public List<LineSegment> getGeometryAtPoint(PointF point)
	{
		List<LineSegment> geo = getGeometry();
		List<LineSegment> ret = new ArrayList<LineSegment>();
		
		for(LineSegment seg : geo){
			if(FloatHelper.areEqual(seg.start, point)){
				ret.add(seg);
			}else if(FloatHelper.areEqual(seg.end, point)){
				ret.add(new LineSegment(seg.end, seg.start));
			}else if(seg.containsPoint(point)){
					ret.add(new LineSegment(point, seg.start));
					ret.add(new LineSegment(point, seg.end));
			}
			
		}
/*
		//	not sure where duplicates come from, but we should purge them
		List<LineSegment> removeList = new ArrayList<LineSegment>();
		for(int i = 0; i < ret.size(); i++){
			for(int j = i+1; j < ret.size(); j++){
				if(ret.get(i).isOverlaping(ret.get(j)))
					removeList.add(ret.get(j));
			}
		}
		
		ret.removeAll(removeList);
	*/	
		return ret;
	}
	
	/**
	 * If you want to do something clever with the geometry yourself, you can use this method.
	 * @return
	 * All the data in the loaded map as line segments
	 */
	public List<LineSegment> getGeometry(){
		List<LineSegment> ret = new ArrayList<LineSegment>();
		
		for(ArrayList<PointF> path : map.paths){
			for(int i = 0; i < path.size() - 1; i++){
				LineSegment segm = new LineSegment(path.get(i), path.get(i+1));
				ret.add(segm);
			}
		}
		
		return ret;
	}
	
	
	/**
	 * A helper method. Call this in your Activity's onCreateContextMenu() method. Pass it all the same parameters
	 * @param menu
	 * @param v
	 * @param menuInfo
	 */
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		menu.add(ContextMenu.NONE, SET_LOCATION_ID, ContextMenu.NONE, "Set Location to here.");
		menu.add(ContextMenu.NONE, SET_DESTINATION_ID, ContextMenu.NONE, "Set destination to here.");
	}
	
	/**
	 * A helper method. Call this in your Activity's onContextItemSelected() method. Pass it all the same parameters.
	 * 
	 * @param item
	 * @return true if the selected item is understood by the Mapper. False otherwise.
	 */
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		boolean ret = true;
		
		if(id == SET_LOCATION_ID){
			startPoint.set(selectPoint);
			
			for(IMapperListener listen : listeners)
				listen.locationChanged(this, startPoint);
			
		}else if(id == SET_DESTINATION_ID){
			destPoint.set(selectPoint);
			
			for(IMapperListener listen : listeners)
				listen.DestinationChanged(this, destPoint);
					
		}else{
			ret = false;
		}
		invalidate();
		
		
		return ret;
	}
	

	
	private class MapperGestureDetector extends SimpleOnGestureListener
	{

		
		Mapper parent;
		public MapperGestureDetector(Mapper mapper) {
			parent = mapper;
		}

		// I am not sure why this needs to return true. It looks like it might eat downs that are not used elsewhere?
		// Not having it, means that onLongPress is called for every click.
		public boolean onDown(MotionEvent e) {
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			
			parent.selectPoint.x = e.getX() / scale.x;
			parent.selectPoint.y = e.getY() / scale.y;
			parent.invalidate();
		}

	}

}
