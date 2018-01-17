package edu.vt.bi.google;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2Polygon;

public class Victoria {
	
	private static void writeTxtToFile(ArrayList<S2Feature> features, HashSet<String> attrToOutput, String outputFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		
		for (S2Feature feature : features) {
			
			// Write the header line (pound followed by CSV attribute:value pairs
			StringBuffer header = new StringBuffer("# ");
			boolean first = true;
			for (String attr : attrToOutput) {
				String val = feature.getAttributes().get(attr);
				if (first) {
					header.append(attr + ":" + val);
					first = false;
				} else {
					header.append("," + attr + ":" + val);
				}
			}
			writer.write(header.toString() + "\n");
			
			// Write cells
			StringBuffer cellBuf = new StringBuffer();
			ArrayList<String> cellTokens = feature.getCellIds().getTokens();
			for (String cellToken : cellTokens) {
				cellBuf.append(cellToken + "\n");
			}
			writer.write(cellBuf.toString());
		}
	     
	    writer.close();
	}

	public static void main(String[] args) throws IOException {

		String sourceFile = "data/VIC_LGA_POLYGON_shp.shp";
		String filterCQL = null;
		int targetLevel = 13;
		boolean flatten = false;
		boolean settleDisputes = true;
		boolean interiorCoveringOnly = false;
		boolean writeCellsToFile = true;
		String outputFile = "data/vic_cells.txt";
		
		HashSet<String> attrToIgnore = new HashSet<String>();
		attrToIgnore.add("the_geom");
		
		HashSet<String> attrToOutput = new HashSet<String>();
		attrToOutput.add("LGA_PID");
		attrToOutput.add("VIC_LGA__2");

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

		System.out.println("Creating map base layer...");
		
		// Create a map content and add our shapefile to it
		MapContent map = new MapContent();
		map.setTitle("S2 Covering Check");

		Style style = SLD.createSimpleStyle(featureSource.getSchema());
		Layer layer = new FeatureLayer(featureSource, style);
		map.addLayer(layer);
		
		
		// Iterate the features with a filter and put the features into a list.
		System.out.println("Getting features and converting shapes to S2Polygons...");

		ArrayList<S2Feature> s2features = new ArrayList<S2Feature>();
		try {
			s2features = GeoToolsWrapper.featuresToS2Features(featureSource, filterCQL, attrToIgnore);
		} catch (CQLException e1) {
			e1.printStackTrace();
		}
		
		// Get coverings for the features
		System.out.println("Getting coverings for "+s2features.size()+" polygons...");
		
		for (S2Feature feature : s2features) {
			ArrayList<S2CellId> covering = S2Wrapper.getCovering(feature.getS2poly(), targetLevel, interiorCoveringOnly);
			feature.setCellIds(new S2CellIdSet(covering));
		}
		
		// Settle disputes means that each cell only belongs to one polygon.  The poly containing the majority of the cell wins.
		if (settleDisputes) {
			
			System.out.println("Settling disputes...");

			// for each pair-wise shape, find the disputed cells
			for (int i=0; i<s2features.size()-1; i++) {
				for (int j=i+1; j<s2features.size(); j++) {
					S2Polygon poly1 = s2features.get(i).getS2poly();
					S2Polygon poly2 = s2features.get(j).getS2poly();
					ArrayList<S2CellId> disputedCells = S2Wrapper.getDisputedCells(poly1, poly2, targetLevel);

					// for each cell, see who this one belongs to based on area
					for (S2CellId cell : disputedCells) {
						double poly1area = S2Wrapper.getFractionOfCellWithin(cell, poly1);
						double poly2area = S2Wrapper.getFractionOfCellWithin(cell, poly2);	
						if (poly1area < poly2area) {
							s2features.get(i).getCellIds().remove(cell);
						} else {
							s2features.get(j).getCellIds().remove(cell);
						}
					}

				}
			}
		}
		
		
		// Write a file with the features and cell ids
		if (writeCellsToFile) {
			System.out.println("Writing cell ids to file...");
			Victoria.writeTxtToFile(s2features, attrToOutput, outputFile);
		}
		
		
		// Now add things to the map
		System.out.println("Adding polygons to the map...");
		
		if (flatten) {
			
			System.out.println("Unioning all cells...");
			
			// union all of the cells
			S2CellIdSet allCells = new S2CellIdSet();
			for (S2Feature feature : s2features) {
				allCells.addAll(feature.getCellIds());
			}

			// put the cells on a layer and add them to the map
			Layer cellLayer = S2CoveringDisplay.getCellLayer(allCells.getCellIds(), Color.BLUE);
			map.addLayer(cellLayer);

		} else {
			System.out.println("Adding layers to map for each shape...");
			
			// create a layer for each polygon
			for (S2Feature feature : s2features) {
				
				// put the cells on a layer and the layer on the map
				ArrayList<S2CellId> covering = feature.getCellIds().getCellIds();
				Layer cellLayer = S2CoveringDisplay.getCellLayer(covering, S2CoveringDisplay.getRandomColor());
				map.addLayer(cellLayer);
			}
		}
		
		// Now display the map
		System.out.println("Showing map...");
		JMapFrame.showMap(map);

	}

}
