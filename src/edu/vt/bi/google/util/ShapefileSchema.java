package edu.vt.bi.google.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

public class ShapefileSchema {

	public static void main(String[] args) {


		String sourceFile = "data/tl_2015_us_zcta510.shp";
		
		// Get the file
		FileDataStore store = null;
		SimpleFeatureSource featureSource = null;
		try {
			store = FileDataStoreFinder.getDataStore(new File(sourceFile));
			featureSource = store.getFeatureSource();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// show the schema
		SimpleFeatureType schema = featureSource.getSchema();
		System.out.println("Schema ("+schema.getAttributeCount()+" attributes): ");
		
		List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
		for (AttributeDescriptor attr : attributes) {
			System.out.println("  " + attr.getLocalName() + " ("+attr.getType()+")");
		}
		

	}

}
