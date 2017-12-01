package edu.vt.bi.google;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.geometry.S2CellId;

public class S2CellIdSet {
	
	private HashSet<String> set;

	public S2CellIdSet() {
		super();
		this.set = new HashSet<String>();
	}
	
	public S2CellIdSet(ArrayList<S2CellId> cells) {
		super();
		this.set = new HashSet<String>();
		this.addAll(cells);
	}
	
	public S2CellIdSet addToken(String token) {
		this.set.add(token);
		return this;
	}
	
	public S2CellIdSet add(S2CellId cellId) {
		this.set.add(cellId.toToken());
		return this;
	}
	
	public S2CellIdSet addAll(S2CellIdSet set2) {
		this.set.addAll(set2.getTokens());
		return this;
	}
	
	public S2CellIdSet addAll(ArrayList<S2CellId> cells) {
		for (S2CellId cell : cells) {
			this.set.add(cell.toToken());
		}
		return this;
	}
	
	public S2CellIdSet removeToken(String token) {
		this.set.remove(token);
		return this;
	}
	
	public S2CellIdSet remove(S2CellId cellId) {
		this.set.remove(cellId.toToken());
		return this;
	}
	
	public S2CellIdSet removeAll(S2CellIdSet set2) {
		this.set.removeAll(set2.getTokens());
		return this;
	}
	
	public S2CellIdSet removeAll(ArrayList<S2CellId> cells) {
		for (S2CellId cell : cells) {
			this.set.remove(cell.toToken());
		}
		return this;
	}
	
	public ArrayList<String> getTokens() {
		return new ArrayList<String>(this.set);
	}
	
	public ArrayList<S2CellId> getCellIds() {
		ArrayList<S2CellId> cellIds = new ArrayList<S2CellId>();
		for (String token : this.set) {
			cellIds.add(S2CellId.fromToken(token));
		}
		return cellIds;
	}
	
	public boolean contains(S2CellId cell) {
		return this.set.contains(cell.toToken());
	}
	
	public boolean containsToken(String token) {
		return this.set.contains(token);
	}
	
	public static S2CellIdSet union(S2CellIdSet set1, S2CellIdSet set2) {
		S2CellIdSet set3 = new S2CellIdSet();
		set3.addAll(set1);
		set3.addAll(set2);
		return set3;
	}
	
	public static S2CellIdSet intersection(S2CellIdSet set1, S2CellIdSet set2) {
		S2CellIdSet smaller = null;
		S2CellIdSet larger = null;
		if (set1.set.size() <= set2.set.size()) {
			smaller = set1;
			larger = set2;
		} else {
			smaller = set2;
			larger = set1;
		}
		
		S2CellIdSet intersection = new S2CellIdSet();
		for (String token : smaller.getTokens()) {
			if (larger.containsToken(token)) {
				intersection.addToken(token);
			}
		}
		return intersection;
	}

}
