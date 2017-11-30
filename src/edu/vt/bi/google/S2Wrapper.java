package edu.vt.bi.google;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

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
	
	public static ArrayList<S2CellId> ensureLevel(ArrayList<S2CellId> cells, int targetLevel) {
		HashSet<S2CellId> l_cells = new HashSet<S2CellId>();
		for (S2CellId cell : cells) {
			if (cell.level() < targetLevel) {
				S2Cell gchildren[] = S2Wrapper.getChildren(new S2Cell(cell), targetLevel - cell.level());
				for (S2Cell gchild : gchildren) {
					l_cells.add(gchild.id());
				}
			} else if (cell.level() > targetLevel) {
				l_cells.add(cell.parent(targetLevel));
			} else {
				l_cells.add(cell);
			}
		}
		return new ArrayList<S2CellId>(l_cells);
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
		ArrayList<S2CellId> cells = nycCovering.cellIds();
		ArrayList<S2CellId> l13_cells = S2Wrapper.ensureLevel(cells, targetLevel);

		for (S2CellId cell : l13_cells) {
			System.out.println(cell.toToken() + "(level " + cell.level() + ")");
		}

		System.out.println("==========================");

		// Now... take a custom polygon and get the covering for it
		ArrayList<S2Polygon> s2polys = null;
		try {
			s2polys = ShpToS2.convertShapesToS2Polygon(new File("data/adm2.shp"),
					"NAME_2 = 'Montgomery' and ID_1 = 47");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Montgomery County, VA cells:");
		for (S2Polygon poly : s2polys) {
			S2CellUnion mcCovering = coverer.getCovering(poly);
			ArrayList<S2CellId> l13_mc_cells = S2Wrapper.ensureLevel(mcCovering.cellIds(), targetLevel);
			for (S2CellId cell : l13_mc_cells) {
				System.out.println(cell.toToken() + "(level " + cell.level() + ")");
			}
		}
		
		System.out.println("==========================");
		
		
		// Now to display a covering on a map...

	}

}
