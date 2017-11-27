package edu.vt.bi.google;
import java.util.ArrayList;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;
import com.google.common.geometry.S2RegionCoverer;

public class S2Wrapper {
	
	public static S2Cell[] getChildren(S2Cell parent, int levels) {
		if (levels == 1) {
			S2Cell children[] = new S2Cell[4];
		    for (int i=0; i<4; i++) {
		    	children[i] = (S2Cell) parent.clone();
		    }
		    parent.subdivide(children);
		    return children;
		} else {
			S2Cell children[] = S2Wrapper.getChildren(parent, 1);
			int numGrandChildren = (int)Math.pow(4, levels);
			int numChildrenPerChild =  (int)Math.pow(4, levels-1);
			S2Cell grandChildren[] = new S2Cell[numGrandChildren];
			for (int i=0; i<children.length; i++) {
				System.arraycopy(S2Wrapper.getChildren(children[i], levels-1), 0, grandChildren, i*numChildrenPerChild, numChildrenPerChild);
		    }
			return grandChildren;
		}
	}

	public static void main(String[] args) {
		
		// Get a specific cell
		S2LatLng bi = S2LatLng.fromDegrees(37.220994, -80.426176);
		S2CellId bi_cell = S2CellId.fromLatLng(bi);
		System.out.println("BI cell: " + bi_cell.toToken() + " (at level 13: "+bi_cell.parent(13).toToken()+")");
		System.out.println("==========================");
		
		// Get the lat/lon centroid from a cell id
		String r_token = "89c25a24";
		S2Cell r_cell = new S2Cell(S2CellId.fromToken(r_token));
		System.out.println(r_token + ":" + r_cell.getCenter().toDegreesString());
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
 	    coverer.setMaxCells(1000);
 	    coverer.setMinLevel(targetLevel);
 	    coverer.setMaxLevel(targetLevel);
 	    
 	    // rectangles are LL(lat,lon), UR(lat,lon)
 	    S2LatLngRect nycRect = new S2LatLngRect(
 	    		S2LatLng.fromDegrees(40.500374, -74.256904), 
 	    		S2LatLng.fromDegrees(40.897490, -73.771038));
 	    S2CellUnion covering = coverer.getCovering(nycRect);
 	    
 	    System.out.println("NYC cells:");
 	    ArrayList<S2CellId> cells = covering.cellIds();
 	    ArrayList<S2CellId> l13_cells = new ArrayList<S2CellId>();
 	    for (S2CellId cell : cells) {
 	    	if (cell.level() < targetLevel) {
 	    		S2Cell gchildren[] = S2Wrapper.getChildren(new S2Cell(cell), targetLevel-cell.level());
 	    		for (S2Cell gchild : gchildren) {
 	    			l13_cells.add(gchild.id());
 	    		}
 	    	} else {
 	    		l13_cells.add(cell);
 	    	}
 	    }
 	    
 	   for (S2CellId cell : l13_cells) {
	    	System.out.println(cell.toToken() + "(level "+cell.level()+")");
	    }
 	        
 	    System.out.println("==========================");
	
 	    
 	    // Now... take a custom polygon and get the covering for it
 	    
 	    // Then, make a git repo out of this!
 	    
	}

}
