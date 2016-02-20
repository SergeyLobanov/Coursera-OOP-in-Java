package module5;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PGraphics;

import java.util.List;

/** Implements a visual marker for ocean earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public class OceanQuakeMarker extends EarthquakeMarker {
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = false;
	}
	

	/** Draw the earthquake as a square */
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.rect(x-radius, y-radius, 2*radius, 2*radius);
		/*if(getClicked()){
			//drawLine(pg, )
			float r = 10*(float)(threatCircle());
			//pg.fill(0);
			pg.noStroke();
			pg.ellipse(x, y, r, r);
		}*/
	}

	/*public float[] drawLine(/*PGraphics pg, List<Marker> markers){
		float [] xy = new float[2];
		for(Marker marker : markers){
			xy[0] = marker.getLocation().getLat();
			xy[1] = marker.getLocation().getLon();
		}
		return xy;
	}*/
	

	

}
