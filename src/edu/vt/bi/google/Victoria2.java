package edu.vt.bi.google;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

public class Victoria2 {


	public static void main(String[] args) throws IOException, CQLException {

		String sourceFile = "data/VIC_LGA_POLYGON_shp.shp";
//		String filterCQL = null;


		System.out.println("Reading file...");

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
		
		// Get the features and filter
//		Filter filter;
//		SimpleFeatureCollection featureCollection;
//
//		if (filterCQL != null) {
//			filter = CQL.toFilter(filterCQL);
//			featureCollection = featureSource.getFeatures(filter);
//		} else {
//			featureCollection = featureSource.getFeatures();
//		}

		// get the schema, find the attribute names, ignore those in the ignore list
		SimpleFeatureType schema = featureSource.getSchema();
		List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
		for (AttributeDescriptor attr : attributes) {
			String attrName = attr.getLocalName();
			System.out.println(attrName);
		}

	}

}
