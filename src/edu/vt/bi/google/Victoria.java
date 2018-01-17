package edu.vt.bi.google;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2Polygon;

public class Victoria {

	public static void main(String[] args) throws IOException {

		String sourceFile = "data/VIC_LGA_POLYGON_shp.shp";
		String filterCQL = null;
		int targetLevel = 13;
		boolean flatten = false;
		boolean settleDisputes = false;

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
		

		
		// XXX maybe this is easier to make a feature wrapper class.  then iterate the features
		// once with a filter and put the features in an ArrayList.  Each feature can have an
		// attribute hashmap, an S2poly, and maybe a set of cells.
		System.out.println("Getting features and converting shapes to S2Polygons...");
		HashSet<String> attrToIgnore = new HashSet<String>();
		attrToIgnore.add("the_geom");
		ArrayList<S2Feature> s2features;
		try {
			s2features = GeoToolsWrapper.featuresToS2Features(featureSource, filterCQL, attrToIgnore);
		} catch (CQLException e1) {
			e1.printStackTrace();
		}
		

		System.out.println("Getting coverings for "+s2features.size()+" polygons...");
		// Get coverings and show them as layers
		// Get a coverage at level 13
		
		// XXX now we need to convert the rest to using the features.  get coverings for all the features, do the layers, sort out the disputes
		
		if (flatten) {
			
			System.out.println("Unioning all cells...");
			
			// union all of the cells, then ensure they are at the target level
			ArrayList<S2CellId> allCells = new ArrayList<S2CellId>();
			for (S2Polygon poly : s2polys) {
				allCells.addAll(S2Wrapper.getCovering(poly, targetLevel, false));
			}

			// put the cells on a layer and add them to the map
			Layer cellLayer = S2CoveringDisplay.getCellLayer(allCells, Color.BLUE);
			map.addLayer(cellLayer);

		} else {
			if (settleDisputes) {
				
				System.out.println("Settling disputes...");

				// get all the coverings as cell id sets
				ArrayList<S2CellIdSet> cellIds = new ArrayList<S2CellIdSet>(s2polys.size());
				for (S2Polygon poly : s2polys) {
					ArrayList<S2CellId> covering = S2Wrapper.getCovering(poly, targetLevel, false);
					cellIds.add(new S2CellIdSet(covering));
				}

				// for each pair-wise shape, find the disputed cells
				for (int i=0; i<s2polys.size()-1; i++) {
					for (int j=i+1; j<s2polys.size(); j++) {
						S2Polygon poly1 = s2polys.get(i);
						S2Polygon poly2 = s2polys.get(j);
						ArrayList<S2CellId> disputedCells = S2Wrapper.getDisputedCells(poly1, poly2, targetLevel);

						// for each cell, see who this one belongs to based on area
						for (S2CellId cell : disputedCells) {
							double poly1area = S2Wrapper.getFractionOfCellWithin(cell, poly1);
							double poly2area = S2Wrapper.getFractionOfCellWithin(cell, poly2);	
							if (poly1area < poly2area) {
								cellIds.get(i).remove(cell);
							} else {
								cellIds.get(j).remove(cell);
							}
						}

					}
				}

				// for each covering, add a layer to the map
				for (S2CellIdSet set : cellIds) {
					Layer cellLayer = S2CoveringDisplay.getCellLayer(set.getCellIds(), S2CoveringDisplay.getRandomColor());
					map.addLayer(cellLayer);
				}

			} else {
				System.out.println("Adding layers to map for each shape...");
				
				// create a layer for each polygon
				for (S2Polygon poly : s2polys) {
					ArrayList<S2CellId> covering = S2Wrapper.getCovering(poly, targetLevel, false);

					// put the cells on a layer and the layer on the map
					Layer cellLayer = S2CoveringDisplay.getCellLayer(covering, S2CoveringDisplay.getRandomColor());
					map.addLayer(cellLayer);
				}
			}
		}
		
		System.out.println("Showing map...");

		// Now display the map
		JMapFrame.showMap(map);

	}

}
