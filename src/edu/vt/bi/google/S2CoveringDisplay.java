package edu.vt.bi.google;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeatureType;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2RegionCoverer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class S2CoveringDisplay {
	
	public static Layer getCellLayer(ArrayList<S2Polygon> s2polys, S2RegionCoverer coverer, int targetLevel){

	    SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
	    b.setName( "s2polys" );
	    b.setCRS( DefaultGeographicCRS.WGS84 ); 
	    b.add( "geom", Polygon.class );
	    b.add( "token", String.class );

	    final SimpleFeatureType TYPE = b.buildFeatureType();

	    DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);
	   	
	    for (S2Polygon poly : s2polys) {
			S2CellUnion mcCovering = coverer.getCovering(poly);
			ArrayList<S2CellId> l13_mc_cells = S2Wrapper.ensureLevel(mcCovering.cellIds(), targetLevel);
			for (S2CellId cell : l13_mc_cells) {
				S2Cell c = new S2Cell(cell);
								
				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
				Coordinate[] coords = new Coordinate[5];
				for (int i=0; i<4; i++) {
					S2LatLng v = new S2LatLng(c.getVertex(i));
					coords[i] = new Coordinate(v.lngDegrees(), v.latDegrees());
				}
				coords[4] = coords[0];
				Polygon p = geometryFactory.createPolygon(coords);

		        featureCollection.add(SimpleFeatureBuilder.build(TYPE, new Object[]{p,cell.toToken()}, null));
			}
	    }
	    Style style = SLD.createPolygonStyle(Color.BLUE, Color.BLUE, 0.3f);

	    Layer cellLayer = new FeatureLayer(featureCollection, style);
	    cellLayer.setTitle("Cell Layer");
	    return cellLayer;
	}

	public static void main(String[] args) {

		String sourceFile = "data/adm2.shp";
		String filterCQL = "NAME_2 = 'Montgomery' and ID_1 = 47";

		// Get the file
		FileDataStore store = null;
		SimpleFeatureSource featureSource = null;
		try {
			store = FileDataStoreFinder.getDataStore(new File(sourceFile));

			// Get the features and filter
			featureSource = store.getFeatureSource();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		// Create a map content and add our shapefile to it
		MapContent map = new MapContent();
		map.setTitle("Quickstart");

		Style style = SLD.createSimpleStyle(featureSource.getSchema());
		Layer layer = new FeatureLayer(featureSource, style);
		map.addLayer(layer);

		// Get a coverage at level 13
		int targetLevel = 13;
		S2RegionCoverer coverer = new S2RegionCoverer();
		coverer.setMaxCells(10000);
		coverer.setMinLevel(targetLevel);
		coverer.setMaxLevel(targetLevel);

		ArrayList<S2Polygon> s2polys = null;
		try {
			s2polys = ShpToS2.convertShapesToS2Polygon(new File(sourceFile), filterCQL);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Layer cellLayer = S2CoveringDisplay.getCellLayer(s2polys, coverer, targetLevel);
		map.addLayer(cellLayer);

		// Now display the map
		JMapFrame.showMap(map);

	}

}
