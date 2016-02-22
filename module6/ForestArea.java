package module6;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google.GoogleMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

import java.util.*;

/**
 * Visualizes forest area in different countries.
 * 
 * It loads the country shapes from a GeoJSON file via a data reader, and loads the population density values from
 * another CSV file (provided by the World Bank). The data value is encoded to transparency via a simplistic linear
 * mapping.
 */
public class ForestArea extends PApplet{

	UnfoldingMap map;
	HashMap<String, Float> forestAreaMap;
	List<Feature> countries;
	List<Marker> countryMarkers;

	Marker obj;

	public void setup() {
		size(850, 650, OPENGL);
		map = new UnfoldingMap(this, 175, 25, 650, 600, new GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);

		// Load lifeExpectancy data
		forestAreaMap = ParseFeed.loadForestAreaFromCSV(this,"ForestAreaWorldBank.csv");

		// Load country polygons and adds them as markers
		countries = GeoJSONReader.loadData(this, "countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		map.addMarkers(countryMarkers);

		shadeCountries();
	}

	public void draw() {
		// Draw map tiles and country markers
		background(200, 230, 255);
		map.draw();
		showTitle(mouseX, mouseY);

		addKey();

	}

	private void addKey(){
		fill(255, 250, 240);
		rect(25, 25, 125, 240);

		fill(0);
		textAlign(CENTER, CENTER);
		textSize(12);
		text("Forest area\n(% of land area):\nlast measuring", 85, 60);
		line(25,100, 150, 100);

		int x = 45, y = 155, z = 60;

		textAlign(LEFT, CENTER);
		text("Country\ncolor", x-10, y-25);
		text("Area\npercent", x+50, y-25);
		text("-", x+z, y+78);
		text(0, x + z, y+3);
		text(20, x + z, y+18);
		text(40, x + z, y+33);
		text(60, x + z, y+48);
		text(80, x + z, y+63);
		fill(150, 70, 0);
		rect(x, y, 10, 10);
		fill(255, 255, 200);
		rect(x, y+15, 10, 10);
		fill(153, 255, 153);
		rect(x, y+30, 10, 10);
		fill(0, 200, 0);
		rect(x, y+45, 10, 10);
		fill(0, 150, 0);
		rect(x, y+60, 10, 10);
		fill(150);
		rect(x, y+75, 10, 10);
	}

	//Helper method to color each country based on life expectancy
	//Red-orange indicates low (near 40)
	//Blue indicates high (near 100)
	private void shadeCountries() {
		for (Marker marker : countryMarkers) {
			// Find data for country of the current marker
			String countryId = marker.getId();
			//System.out.println(forestAreaMap.containsKey(countryId));
			if (forestAreaMap.containsKey(countryId)) {
				float areaPercent = forestAreaMap.get(countryId);
				// Encode value as brightness (values range: 40-90)
				int colorLevel = (int) map(areaPercent, 0, 100, 0, 250);
				if (areaPercent < 35) {
					marker.setColor(color(colorLevel + 150, 75 + colorLevel * 2, colorLevel * 2, 230));
					/*if (obj!=null && obj.isSelected()) { //to change shade of selected country
						obj.setColor(color(colorLevel + 150, 75 + colorLevel * 2, colorLevel * 2, 200));
					}*/
				} else {
					marker.setColor(color(200 - colorLevel, 350 - colorLevel, 200 - colorLevel, 230));
					/*if (obj!=null && obj.isSelected()) {
						obj.setColor(color(200 - colorLevel, 350 - colorLevel, 200 - colorLevel, 200));
					}*/
				}
			}
			else {
				marker.setColor(color(150));
			}
		}
	}

	public void mouseMoved() {
		if(obj != null){
			obj.setSelected(false);
			obj = null;
		}

		selectMarkerIfHover(countryMarkers);

	}

	private void selectMarkerIfHover(List<Marker> markers)	{
		for(Marker marker : markers){
			if(marker.isInside(map, mouseX, mouseY)){
				marker.setSelected(true);
				obj = marker;
			}
		}
	}

	public void showTitle(float x, float y)
	{
		if(obj==null) return;
		if(obj.isSelected()) {
			String title = "Forest area (% of land area) \n";
			fill(255, 255, 255, 150);
			rect(x + 13, y - 9, textWidth(title) + 5, 32);
			fill(0);
			String name = (String) (obj.getProperty("name"));
			if(forestAreaMap.containsKey(obj.getId())) {
				float areaPercent = forestAreaMap.get(obj.getId());
				text( title + name + ": " + areaPercent, x + 15, y + 5);
			} else
				text(title + name + ": have not data", x + 15, y + 5);
		}
	}


}
