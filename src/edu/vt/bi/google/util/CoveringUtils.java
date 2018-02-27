package edu.vt.bi.google.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQLException;

import com.google.common.geometry.S2CellId;

import edu.vt.bi.google.GeoToolsWrapper;
import edu.vt.bi.google.S2Feature;
import edu.vt.bi.google.S2Wrapper;

public class CoveringUtils {
	
	public static void getCovering(String shapeFile, String filterCQL, int minLevel, int maxLevel, boolean interiorCoveringOnly, BufferedWriter buf) throws IOException {
		
		HashSet<String> attrToIgnore = new HashSet<String>();
		attrToIgnore.add("the_geom");

		// Get the file
		FileDataStore store = null;
		SimpleFeatureSource featureSource = null;
		try {
			store = FileDataStoreFinder.getDataStore(new File(shapeFile));

			// Get the features and filter
			featureSource = store.getFeatureSource();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Iterate the features with a filter and put the features into a list.
		ArrayList<S2Feature> s2features = new ArrayList<S2Feature>();
		try {
			if (filterCQL.length() < 1) filterCQL = null;
			s2features = GeoToolsWrapper.featuresToS2Features(featureSource, filterCQL, attrToIgnore);
		} catch (CQLException e1) {
			e1.printStackTrace();
		}


		// Get coverings for the features
		for (S2Feature feature : s2features) {

			ArrayList<S2CellId> covering = new ArrayList<S2CellId>();
			if (minLevel == maxLevel) {
				covering = S2Wrapper.getCovering(feature.getS2poly(), minLevel, interiorCoveringOnly);
			} else {
				covering = S2Wrapper.getCovering(feature.getS2poly(), minLevel, maxLevel, interiorCoveringOnly);
			}
			
			for (S2CellId cell : covering) {
				buf.write(cell.toToken()+"\n");
			}

		}
		
		buf.flush();
	}

	public static String getUsage() {
		StringBuffer buf = new StringBuffer();
		buf.append("Usage: java -jar coveringutil.jar <function> <shapefile> <filterCQL> [<targetLevel> OR <minlevel> <maxlevel>]\n");
		buf.append("  Example: java -jar coveringutil.jar getCovering data/tl_2015_us_zcta510.shp \"ZCTA5CE10 = 24060\" 15\n");
		buf.append("  Example: java -jar coveringutil.jar getCovering data/tl_2015_us_zcta510.shp \"ZCTA5CE10 = 24060\" 15 25\n");
		buf.append("  Example: java -jar coveringutil.jar getInteriorCovering data/tl_2015_us_zcta510.shp \"ZCTA5CE10 = 24060\" 13\n");
		buf.append("  Example: java -jar coveringutil.jar getInteriorCovering data/tl_2015_us_zcta510.shp \"ZCTA5CE10 = 24060\" 13 25\n");
		return buf.toString();
	}
	
	
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println(CoveringUtils.getUsage());
			System.exit(0);
		} 

		String function = args[0];
		String sourceFile = args[1];
		String filterCQL = args[2];
		int minLevel = Integer.parseInt(args[3]);
		
		int maxLevel = minLevel;
		if (args.length == 5) maxLevel = Integer.parseInt(args[4]);
		if (maxLevel > 30) maxLevel = 30;
		
		boolean interiorCoveringOnly = false;
			
		switch (function) {
			case "getCovering":
				break;
								
			case "getInteriorCovering":
				interiorCoveringOnly = true;
				break;
				
			default:
				System.out.println(CoveringUtils.getUsage());
				break;
		}
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
		try {
			CoveringUtils.getCovering(sourceFile, filterCQL, minLevel, maxLevel, interiorCoveringOnly, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
