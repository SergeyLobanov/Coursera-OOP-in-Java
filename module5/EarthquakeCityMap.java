package module5;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setup and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;//with CommonMarker type my version isn't work
	private CommonMarker lastClicked;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);

		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    //printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);

	}  // End setup
	
	
	public void draw() {
		background(255, 200, 0);//1
		mouseReleased();
		map.draw();//2
		addKey();//3

		drawButtons();//+4

	}
	//helper method for buttons
	private void drawButtons(){
		fill(255);
		rect(250, 100, 25, 25);

		fill(100);
		rect(250, 150, 25, 25);
	}
	//for practice
	public void mouseReleased(){
		if(mouseX > 250 && mouseX < 275 && mouseY > 100 && mouseY < 125){
			background(255);
		} else if(mouseX > 250 && mouseX < 275 && mouseY > 150 && mouseY < 175){
			background(100);
		}
	}
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	// If there is a marker under the cursor, and lastSelected is null 
	// set the lastSelected to be the first marker found under the cursor
	// Make sure you do not select two markers.
	// 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// TODO: Implement this method
		for(Marker marker : markers){
			if(marker.isInside(map, mouseX, mouseY)  && lastSelected == null){
				lastSelected = (CommonMarker)marker;
				marker.setSelected(true);
			}
		}
	}
	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		// TODO: Implement this method
		// Hint: You probably want a helper method or two to keep this code
		// from getting too long/disorganized
		if(lastClicked != null){
			unhideMarkers();
			lastClicked = null;
		}else {
			checkEarthquakesForClick();
			checkCitiesForClick();
		}
	}

	private void checkEarthquakesForClick(){
		if(lastClicked != null) return;
		for(Marker markerQ : quakeMarkers){
			if(markerQ.isInside(map, mouseX, mouseY) && !markerQ.isHidden()){
				lastClicked = (CommonMarker) markerQ;
				for(Marker markerHQ : quakeMarkers){
					if(lastClicked != markerHQ)
						markerHQ.setHidden(true);
				}
				for(Marker markerC : cityMarkers){
					if(markerC.getDistanceTo(lastClicked.getLocation()) > ((EarthquakeMarker)lastClicked).threatCircle()){
						markerC.setHidden(true);
					}
				}
				return;
			}
		}
	}
	private void checkCitiesForClick(){
		if(lastClicked != null) return;
		for(Marker markerC : cityMarkers){
			if(markerC.isInside(map, mouseX, mouseY) && !markerC.isHidden()){
				lastClicked = (CommonMarker) markerC;
				for(Marker markerHC : cityMarkers){
					if(lastClicked != markerHC)
						markerHC.setHidden(true);
				}
				for(Marker markerQ : quakeMarkers){
					if(lastClicked.getDistanceTo(markerQ.getLocation()) > ((EarthquakeMarker)markerQ).threatCircle()){
						markerQ.setHidden(true);
					}
				}
				return;
			}
		}
	}





	// loop over and unhide all markers
	private void unhideMarkers() { //()
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		rect(25, 50, 150, 250);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);

		int x = 50,y = 95;
		strokeWeight(2);
		fill(color(255, 0, 0));//50, 125
		triangle(x-5, y+5, x, y-5, x+5, y+5);
		fill(color(255, 255, 255));
		ellipse(x, y+20, 10, 10);
		fill(color(255, 255, 255));
		rect(x-5, y+35, 10, 10);

		fill(color(255, 255, 0));
		ellipse(x, y+80, 10, 10);
		fill(color(0, 0, 255));
		ellipse(x, y+100, 10, 10);
		fill(color(255, 0, 0));
		ellipse(x, y+120, 10, 10);
		fill(color(255, 255, 255));
		ellipse(x, y+140, 10, 10);
		line(x-8, y+132, x+8, y+148);
		line(x+8, y+132, x-8, y+148);

		fill(0, 0, 0);
		text("City Marker", x+15, y);
		text("Land Quake", x+15, y+20);
		text("Ocean Quake", x+15, y+40);

		text("Size ~ magnitude", 50, 155);
		text("Shallow", x+15, y+80);
		text("Intermadiate", x+15, y+100);
		text("Deep", x+15, y+120);
		text("Past hour", x+15, y+140);
			
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.	
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for(Marker countryM : countryMarkers){
			int countQuakes = 0;
			for(Marker earthquakeM : quakeMarkers ){
				if(countryM.getProperty("name") == earthquakeM.getProperty("country")){
					countQuakes++;
				}
			}
			if(countQuakes > 0) {
				totalWaterQuakes -= countQuakes;
				System.out.println(countryM.getProperty("name") + ": " + countQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
