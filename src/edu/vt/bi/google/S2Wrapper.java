package edu.vt.bi.google;

import java.io.File;
import java.util.ArrayList;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2Polygon;
import com.google.common.geometry.S2RegionCoverer;

public class S2Wrapper {

	public static S2Cell[] getChildren(S2Cell parent, int levels) {
		if (levels == 1) {
			S2Cell children[] = new S2Cell[4];
			for (int i = 0; i < 4; i++) {
				children[i] = (S2Cell) parent.clone();
			}
			parent.subdivide(children);
			return children;
		} else {
			S2Cell children[] = S2Wrapper.getChildren(parent, 1);
			int numGrandChildren = (int) Math.pow(4, levels);
			int numChildrenPerChild = (int) Math.pow(4, levels - 1);
			S2Cell grandChildren[] = new S2Cell[numGrandChildren];
			for (int i = 0; i < children.length; i++) {
				System.arraycopy(S2Wrapper.getChildren(children[i], levels - 1), 0, grandChildren,
						i * numChildrenPerChild, numChildrenPerChild);
			}
			return grandChildren;
		}
	}
	
	
	public static ArrayList<S2CellId> ensureLevel(S2CellUnion cells, int targetLevel) {
		ArrayList<S2CellId> denormCells = new ArrayList<S2CellId>();
		cells.denormalize(targetLevel, 1, denormCells);
		return denormCells;
	}
	
	public static S2Point getCellCentroid(String token) {
		S2Cell cell = new S2Cell(S2CellId.fromToken(token));
		return cell.getCenter();
	}
	
	public static String getCellTokenForPoint(double lat, double lon, int level) {
		S2LatLng ll = S2LatLng.fromDegrees(lat, lon);
		S2CellId cell = S2CellId.fromLatLng(ll);
		return cell.parent(level).toToken();
	}

	
	public static ArrayList<S2CellId> getCovering(S2Polygon s2poly, int targetLevel, boolean interiorOnly) {
		
		// Get a coverage at level 13
		S2RegionCoverer coverer = new S2RegionCoverer();
		coverer.setMaxCells(10000);
		coverer.setMinLevel(targetLevel);
		coverer.setMaxLevel(targetLevel);

		S2CellUnion covering = null;
		if (interiorOnly) {
			covering = coverer.getInteriorCovering(s2poly);
		} else {
			covering = coverer.getCovering(s2poly);
		}
		
		ArrayList<S2CellId> cells = S2Wrapper.ensureLevel(covering, targetLevel);
		return cells;
				
	}
	
	public static ArrayList<S2CellId> getBorderCells(S2Polygon s2poly, int targetLevel) {
		ArrayList<S2CellId> covering = S2Wrapper.getCovering(s2poly, targetLevel, false);
		ArrayList<S2CellId> interiorCovering = S2Wrapper.getCovering(s2poly, targetLevel, true);
		
		// subtract the interior covering
		ArrayList<S2CellId> borderCells = new ArrayList<S2CellId>();
		for (S2CellId cell : covering) {
			if (!interiorCovering.contains(cell)) {
				borderCells.add(cell);
			}
		}
		
		return borderCells;
	}
	
	
	public static ArrayList<S2CellId> getDisputedCells(S2Polygon poly1, S2Polygon poly2, int targetLevel) {
		S2CellIdSet poly1Border = new S2CellIdSet(S2Wrapper.getBorderCells(poly1, targetLevel));
		S2CellIdSet poly2Border = new S2CellIdSet(S2Wrapper.getBorderCells(poly2, targetLevel));
		S2CellIdSet disputedCells = S2CellIdSet.intersection(poly1Border, poly2Border);
		return disputedCells.getCellIds();
	}
	
	public static double getFractionWithin(S2Polygon poly, S2CellId cell) {
		
		
		/* There's probably a better way to do this.  Like maybe we can do a covering with a large
		 * level range so that there are little polygons around the outsides.  Then, when aggregating
		 * to the target level, summing the fractions of cells.  So, if the target is level 13, for
		 * any cell >13, we know based on its level what fraction of a level 13 cell is contained in the 
		 * polygon.  Level 17 is 1/256 of the area of the L13 cell.
		 * */
		
		int levelsDown = 4; // gives 256 points of checking
		if (cell.level() > 26) {
			levelsDown = 30 - cell.level();
		}
		S2Cell gchildren[] = S2Wrapper.getChildren(new S2Cell(cell), levelsDown);
		
		long contained = 0;
		long total = 0;
		for (S2Cell gchild : gchildren) {
			if (poly.contains(gchild)) {
				contained++;
			}
			total++;
		}
		System.out.println("contained: " + contained + " total: " + total);
		
		return (double) contained / (double) total;
	}

	public static void main(String[] args) {

		// Get a specific cell
		double lat = 37.220994;
		double lon = -80.426176;
		System.out.println("BI cell: " + S2Wrapper.getCellTokenForPoint(lat, lon, 30) + " (at level 13: " + S2Wrapper.getCellTokenForPoint(lat, lon, 13) + ")");
		System.out.println("==========================");

		// Get the lat/lon centroid from a cell id
		String r_token = "89c25a24";
		S2Point r_centroid = S2Wrapper.getCellCentroid(r_token);
		System.out.println(r_token + ":" + r_centroid.toDegreesString());
		System.out.println("==========================");

		// try getting some children for a cell
		String l9_cell_token = "89c25c";
		int levelsDown = 2;
		S2Cell l9_cell = new S2Cell(S2CellId.fromToken(l9_cell_token));
		S2Cell children[] = S2Wrapper.getChildren(l9_cell, levelsDown);
		System.out.println(l9_cell_token + " has " + children.length + " children at " + levelsDown + " levels down:");
		for (S2Cell child : children) {
			System.out.println(child.id().toToken() + " (level " + child.level() + ")");
		}
		System.out.println("==========================");

		// Get a coverage at level 13
		int targetLevel = 13;
		S2RegionCoverer coverer = new S2RegionCoverer();
		coverer.setMaxCells(10000);
		coverer.setMinLevel(targetLevel);
		coverer.setMaxLevel(targetLevel);

		// rectangles are LL(lat,lon), UR(lat,lon)
		S2LatLngRect nycRect = new S2LatLngRect(S2LatLng.fromDegrees(40.500374, -74.256904),
				S2LatLng.fromDegrees(40.897490, -73.771038));
		S2CellUnion nycCovering = coverer.getCovering(nycRect);

		System.out.println("NYC cells:");
		ArrayList<S2CellId> l13_cells = S2Wrapper.ensureLevel(nycCovering, targetLevel);

		for (S2CellId cell : l13_cells) {
			System.out.println(cell.toToken() + "(level " + cell.level() + ")");
		}

		System.out.println("==========================");

		// Now... take a custom polygon and get the covering for it
		ArrayList<S2Polygon> s2polys = null;
		try {
			s2polys = ShpToS2.convertShapesToS2Polygons(new File("data/adm2.shp"),
					"NAME_2 = 'Montgomery' and ID_1 = 47");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Montgomery County, VA cells:");
		for (S2Polygon poly : s2polys) {
			ArrayList<S2CellId> l13_mc_cells = S2Wrapper.getCovering(poly, targetLevel, false);
			for (S2CellId cell : l13_mc_cells) {
				System.out.println(cell.toToken() + "(level " + cell.level() + ") " + 100.0*S2Wrapper.getFractionWithin(poly, cell) + "% contained");
			}
		}
		
		System.out.println("==========================");
		
		
		// Now to display a covering on a map... see S2CoveringDisplay
		// Maybe it's simpler to have a class that wraps a shape file 
		
		// for two adjacent shapes, figure out which cells on the border should belong to which
		String sourceFile = "data/adm2.shp";
		String filterCQL = "(NAME_2 = 'Montgomery' or NAME_2 = 'Floyd') and ID_1 = 47";
		
		// convert shapes to S2Polygons
		s2polys = null;
		try {
			s2polys = ShpToS2.convertShapesToS2Polygons(new File(sourceFile), filterCQL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		S2Polygon poly1 = s2polys.get(0);
		S2Polygon poly2 = s2polys.get(1);
		ArrayList<S2CellId> disputedCells = S2Wrapper.getDisputedCells(poly1, poly2, targetLevel);
		System.out.println("Cell (poly1area, poly2area)");
		for (S2CellId cell : disputedCells) {
			double poly1area = S2Wrapper.getFractionWithin(poly1, cell);
			double poly2area = S2Wrapper.getFractionWithin(poly2, cell);
			System.out.println(cell.toToken() + " (" + poly1area + "," + poly2area + ")");
		}
		
	
	}

}
