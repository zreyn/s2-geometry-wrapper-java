package edu.vt.bi.google;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Loop;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2PolygonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeoToolsWrapper {
	
	public static ArrayList<S2Feature> featuresToS2Features(SimpleFeatureSource featureSource, String featureFilterCQL, HashSet<String> attributesToIgnore) throws IOException, CQLException {
		
		// setup the feature array list
		ArrayList<S2Feature> s2features = new ArrayList<S2Feature>();

		// Get the features and filter
		Filter filter;
		SimpleFeatureCollection featureCollection;

		if (featureFilterCQL != null) {
			filter = CQL.toFilter(featureFilterCQL);
			featureCollection = featureSource.getFeatures(filter);
		} else {
			featureCollection = featureSource.getFeatures();
		}
		
		// get the schema, find the attribute names, ignore those in the ignore list
		SimpleFeatureType schema = featureSource.getSchema();
		List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
		ArrayList<String> attributeNames = new ArrayList<String>();
		for (AttributeDescriptor attr : attributes) {
			String attrName = attr.getLocalName();
			if (!attributesToIgnore.contains(attrName)) {
				attributeNames.add(attrName);
			}
		}
		
		
		// step through each feature that passed the filter
		SimpleFeatureIterator iterator = featureCollection.features();
		try {
			while (iterator.hasNext()) {
				SimpleFeature feature = iterator.next();	
				
				// setup the feature
				S2Feature s2feature = new S2Feature();
				
				// get the feature's attributes
				HashMap<String,String> featureAttributes = new HashMap<String,String>();
				for (String attributeName : attributeNames) {
					Object attr = feature.getAttribute(attributeName);
					if (attr != null) {
						featureAttributes.put(attributeName, attr.toString());
					}
				}
				s2feature.setAttributes(featureAttributes);
				
				// convert the geometry to S2Polygon
				Geometry geom = (Geometry) feature.getDefaultGeometry();
				S2Polygon s2poly = GeoToolsWrapper.geometryToS2Polygon(geom);
				s2feature.setS2poly(s2poly);
				
				// add it to the array list
				s2features.add(s2feature);

			}
		} finally {
			iterator.close(); // IMPORTANT
		}

		return s2features;
	}

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

		// Get the file
		FileDataStore store = FileDataStoreFinder.getDataStore(sourceFile);

		// Get the features and filter
		SimpleFeatureSource featureSource = store.getFeatureSource();
		return GeoToolsWrapper.shapesToS2Polygons(featureSource, featureFilterCQL);

	}
	
	public static ArrayList<S2Polygon> shapesToS2Polygons(SimpleFeatureSource featureSource, String featureFilterCQL) throws IOException, CQLException {

		// Setup the polygon array list
		ArrayList<S2Polygon> s2polys = new ArrayList<S2Polygon>();

		// Get the features and filter
		Filter filter;
		SimpleFeatureCollection featureCollection;
		
		if (featureFilterCQL != null) {
			filter = CQL.toFilter(featureFilterCQL);
			featureCollection = featureSource.getFeatures(filter);
		} else {
			featureCollection = featureSource.getFeatures();
		}

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
	
	public static MultiPolygon s2PolygonToPolygon(S2Polygon s2Poly) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		
		Polygon polys[] = new Polygon[s2Poly.numLoops()];
		for (int i=0; i< s2Poly.numLoops(); i++) {
			
			S2Loop loop = s2Poly.loop(i);
			
			Coordinate [] coords = new Coordinate[loop.numVertices()+1];
			Coordinate firstCoordInLoop = null;
			for (int j=0; j<loop.numVertices(); j++) {
				S2LatLng v = new S2LatLng(loop.vertex(j));
				Coordinate curr = new Coordinate(v.lngDegrees(), v.latDegrees());
				coords[j] = new Coordinate(v.lngDegrees(), v.latDegrees());	
				
				if (firstCoordInLoop == null) {
					firstCoordInLoop = curr;
				}
	
			}
			
			// close each loop
			coords[loop.numVertices()] = firstCoordInLoop;
			
			polys[i] = geometryFactory.createPolygon(coords);
		}	


		MultiPolygon mp = geometryFactory.createMultiPolygon(polys);
		return mp;
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
