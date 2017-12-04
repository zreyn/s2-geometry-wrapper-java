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
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2PolygonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class GeoToolsWrapper {

	public static S2Polygon geometryToS2Polygon(Geometry g) {
		
		// setup the S2 polygon builder
		S2PolygonBuilder polygonBuilder = new S2PolygonBuilder();
		 
		S2Point firstPoint = null;
		S2Point prevPoint = null;
		for (Coordinate c : g.getCoordinates()) {

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

		// return the polygon 
		return polygonBuilder.assemblePolygon();
	}

	public static ArrayList<S2Polygon> shapesToS2Polygons(File sourceFile, String featureFilterCQL) throws IOException, CQLException {

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
				
				// convert the geometry to S2Polygon and add it to the array list
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				s2polys.add(GeoToolsWrapper.geometryToS2Polygon(geom));

			}
		} finally {
			iterator.close(); // IMPORTANT
		}

		return s2polys;

	}

	public static Polygon s2CellToPolygon(S2Cell cell) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Coordinate[] coords = new Coordinate[5];
		for (int i=0; i<4; i++) {
			S2LatLng v = new S2LatLng(cell.getVertex(i));
			coords[i] = new Coordinate(v.lngDegrees(), v.latDegrees());
		}
		coords[4] = coords[0];
		Polygon p = geometryFactory.createPolygon(coords);

		return p;
	}
	
	public static Polygon s2PolygonToPolygon(S2Polygon s2Poly) {
		// XXX TO DO
		return null;
	}

	public static S2Polygon getS2Intersection(Polygon poly1, Polygon poly2) {
		Geometry intersection = poly1.intersection(poly2);
		return GeoToolsWrapper.geometryToS2Polygon(intersection);
	}

	public static void main(String[] args) throws Exception {

		// for CQL help - http://docs.geotools.org/latest/userguide/library/cql/cql.html

		ArrayList<S2Polygon> s2polys = null;
		try {
			s2polys = GeoToolsWrapper.shapesToS2Polygons(new File("data/adm2.shp"),
					"NAME_2 = 'Citrus' or (NAME_2 = 'Montgomery' and ID_1 = 47)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(s2polys);

	}

}
