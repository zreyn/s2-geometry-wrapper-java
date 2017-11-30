package edu.vt.bi.google;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2PolygonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class ShpToS2 {


	public static ArrayList<S2Polygon> convertShapesToS2Polygon(File sourceFile, String featureFilterCQL) throws IOException, CQLException {

		// Setup the polygon array list
		ArrayList<S2Polygon> s2polys = new ArrayList<S2Polygon>();
		
		// Get the file
		FileDataStore store = FileDataStoreFinder.getDataStore(sourceFile);
		
		// Get the features and filter
		SimpleFeatureSource featureSource = store.getFeatureSource();        
		Filter filter;
		filter = CQL.toFilter(featureFilterCQL);
		SimpleFeatureCollection featureCollection = featureSource.getFeatures(filter);

		// step through each feature that passed the filter
		SimpleFeatureIterator iterator = featureCollection.features();
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();	
				
				// setup the S2 polygon builder
				 S2PolygonBuilder polygonBuilder = new S2PolygonBuilder();
				
				// convert the geometry to S2Polygon
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				S2Point firstPoint = null;
				S2Point prevPoint = null;
				for (Coordinate c : geom.getCoordinates()) {
					
					// convert the coordinates to an S2Point (new projection)
					S2Point currPoint = S2LatLng.fromDegrees(c.y, c.x).toPoint();
					
					// add the edge
					if (firstPoint == null) firstPoint = currPoint;
					if (prevPoint != null) {
						polygonBuilder.addEdge(prevPoint, currPoint);
					} 
					prevPoint = currPoint;
				}
				// close the loop
				polygonBuilder.addEdge(prevPoint, firstPoint);
				
				// put the polygon in the array list
				s2polys.add(polygonBuilder.assemblePolygon());

			}
		} finally {
			iterator.close(); // IMPORTANT
		}

		return s2polys;

	}

	public static void main(String[] args) throws Exception {

		// for CQL help - http://docs.geotools.org/latest/userguide/library/cql/cql.html
		
		ArrayList<S2Polygon> s2polys = null;
		try {
			s2polys = ShpToS2.convertShapesToS2Polygon(new File("data/adm2.shp"),
					"NAME_2 = 'Citrus' or (NAME_2 = 'Montgomery' and ID_1 = 47)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(s2polys);

	}

}
