package edu.vt.bi.google;

import java.util.HashMap;

import com.google.common.geometry.S2Polygon;

public class S2Feature {
	
	private HashMap<String,String> attributes;
	private S2CellIdSet cellIds;
	private S2Polygon s2poly;
	
	
	public S2Feature() {
		super();
	}


	public S2Feature(HashMap<String, String> attributes, S2CellIdSet cellIds, S2Polygon s2poly) {
		super();
		this.attributes = attributes;
		this.cellIds = cellIds;
		this.s2poly = s2poly;
	}


	public HashMap<String, String> getAttributes() {
		return attributes;
	}


	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}


	public S2CellIdSet getCellIds() {
		return cellIds;
	}


	public void setCellIds(S2CellIdSet cellIds) {
		this.cellIds = cellIds;
	}


	public S2Polygon getS2poly() {
		return s2poly;
	}


	public void setS2poly(S2Polygon s2poly) {
		this.s2poly = s2poly;
	}


	@Override
	public String toString() {
		return "S2Feature [attributes=" + attributes + ", cellIds=" + cellIds + ", s2poly=" + s2poly + "]";
	}
	

}
