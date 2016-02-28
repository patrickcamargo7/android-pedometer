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
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.PointF;

/**
 * A helper class to load a map.  
 * @author Kirill
 *
 */
public class MapLoader 
{
	private final static float DEFAULT_SCALE = 0.05f;
	private static DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder DocBuilder = null;
	
	private PointF fileMaxCoord = new PointF();
	private PointF fileScale = new PointF();
	
	PedometerMap pedMap = new PedometerMap();
	
	/**
	 * Create a Pedometer map out of the provided SVG file.
	 * @param dir pass the return from the method getExternalFilesDir(null) to this parameter
	 * @param filename The filename of the map to load
	 * @return a PedometerMap representing the map file that was loaded
	 */
	public PedometerMap loadMap(File dir, final String filename)
	{
		if(DocBuilder == null){
			try {
				DocBuilder = docBuildFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		
		File map = dir.listFiles(new FilenameFilter(){

			public boolean accept(File dir, String name) {
				return name.equals(filename);
			}
		})[0];
		
		Document doc = null;
		
		try {
			doc = DocBuilder.parse(map);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Process metadata
		Element svg = (Element) doc.getElementsByTagName("svg").item(0);

		fileMaxCoord.set(Float.parseFloat(svg.getAttribute("width")),
						Float.parseFloat(svg.getAttribute("height")));
		
	
		String scaleX = svg.getAttribute("xScale");
		String scaleY = svg.getAttribute("yScale");
		
		if(fileScale.x == 0 || fileScale.y == 0){
			fileScale.set(DEFAULT_SCALE, DEFAULT_SCALE);
		}else{
			fileScale.set(Float.parseFloat(scaleX), Float.parseFloat(scaleY));
		}
		
		// process paths
		NodeList filePaths = doc.getElementsByTagName("path");
		
		for(int i = 0; i < filePaths.getLength(); i++){
			pedMap.paths.add(parseConvertPath(filePaths.item(i)));
		}
		return pedMap;
	}
	
	/**
	 * Gets the map this loader has most recently loaded
	 * @return the map
	 */
	public PedometerMap getLastMap()
	{
		return pedMap;
	}
	
	private PointF MakePoint(String s1, String s2)
	{
		return new PointF(
				Float.parseFloat(s1),
				Float.parseFloat(s2)
				) ;
	}
	
	private PointF MakePointReletive(PointF p, String s1, String s2)
	{
		return new PointF(
				p.x + Float.parseFloat(s1),
				p.y + Float.parseFloat(s2)
				) ;
	}
	
	private ArrayList<PointF> parseConvertPath(Node node) {
		Element elem = (Element) node;
		ArrayList<PointF> ret = new ArrayList<PointF>();
		
		String d = elem.getAttribute("d");
		
		String[] pathString = d.split("[ ,]");
		PointF refPoint = new PointF();
		char defaultCommand = 'l';
		
		try{
			for(int i = 0; i < pathString.length; i++){
			PointF newPoint;
			
			if("cCsSqQtTaAmMlLzZ-1234567890".indexOf(pathString[i].charAt(0)) == -1){
				throw new InvalidParameterException("A character that was to be interpretated as a command charcater" +
						"is not known by the Map loader. Check your path Data. The unknown chatacter was: <" + pathString[i].charAt(0) +">" +
						"In the path {" + d + "}");
			}
				
			// I don't handle bezier curves, so skip all the extra components of the bezier commands
			switch(pathString[i].charAt(0)){
			case 'c':
				i += 4;
				pathString[i] = "l";
				break;
			case 'C':
				pathString[i] = "L";
				i+= 4;
				break;
			case 's':
				i += 2;
				pathString[i] = "l";
				break;
			case 'S':
				i += 2;
				pathString[i] = "L";
				break;
			case 'q':
				i += 2;
				pathString[i] = "l";
				break;
			case 'Q':
				i += 2;
				pathString[i] = "L";
				break;
			case 't':
				pathString[i] = "l";
				break;
			case 'T':
				pathString[i] = "L";
				break;
			case 'a':
				i += 5;
				pathString[i] = "l";
				break;
			case 'A':
				i += 5;
				pathString[i] = "L";
				break;
			}
			
			
			// read control character
			switch(pathString[i].charAt(0)){
			case 'M':
				newPoint = MakePoint(pathString[i+1], pathString[i+2]);
				i += 2;
				defaultCommand = 'L';
			break;
			case 'm':
			case 'l':
				newPoint = MakePointReletive(refPoint, pathString[i+1], pathString[i+2]);
				i += 2;
				defaultCommand = 'l';
				break;
			case 'L':
				newPoint = MakePoint(pathString[i+1], pathString[i+2]);
				i += 2;
				defaultCommand = 'L';
				break;
			case 'z':
			case 'Z':
				newPoint = new PointF();
				newPoint.set(ret.get(0));
				break;
			//If there is no control character, we use the last one.
			default:
				switch(defaultCommand){
				case 'l':
				default:
					newPoint = MakePointReletive(refPoint, pathString[i], pathString[i+1]);
					i += 1;
					break;
				case 'L':
					newPoint = MakePoint(pathString[i], pathString[i+1]);
					i += 1;
					break;
				}
			}
			
			ret.add(newPoint);
			refPoint.set(newPoint);
	
									
			}
		}catch(IndexOutOfBoundsException e){
			throw new IndexOutOfBoundsException("There were not enough elements to process all the commands. "+
					"Either the path contains an unknown command or one of the commands has too few parameters" +
					"The path being processed was {" + d +"}");
		}catch(NumberFormatException e){
			throw new NumberFormatException("The map loader encountered a problem parsing path data. This likely means that you have an " +
					"unknown control character. Check your paths" +
					"The path being processed was {" + d +"}");
		}
		for(PointF p : ret)
			convertCoord(p);
		
		return ret;
	}

	/**
	 * Converts the coordinate to real-world space
	 * @param coord
	 */
	private void convertCoord(PointF coord){
		coord.set(coord.x * fileScale.x, coord.y * fileScale.y);
	}
}








