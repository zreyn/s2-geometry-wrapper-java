package edu.vt.bi.google.util;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2Point;

import edu.vt.bi.google.S2Wrapper;

public class CellUtils {

	public static String getCentroid(String token) {
		S2Point centroid = S2Wrapper.getCellCentroid(token);
		return centroid.toDegreesString();
	}
	
	public static String getChildren(String token, String targetLevel) {
		S2Cell cell = S2Wrapper.getCellByToken(token);
		int levels = Integer.parseInt(targetLevel)-cell.level();
		if (levels > 0) {
			StringBuffer buf = new StringBuffer();
			S2Cell children[] = S2Wrapper.getChildren(cell, levels);
			for (S2Cell child : children) {
				buf.append(child.id().toToken() + "\n");
			}
			return buf.toString();
		}
		return "Bad Arguments";
	}
	
	public static String getLevel(String token) {
		S2Cell cell = S2Wrapper.getCellByToken(token);
		return Integer.toString((int)cell.level());
	}
	
	public static String getUsage() {
		StringBuffer buf = new StringBuffer();
		buf.append("Usage: java -jar cellutil.jar <function> <token> [<level>]\n");
		buf.append("  Example: java -jar cellutil.jar getCentroid 883bf7d04\n");
		buf.append("  Example: java -jar cellutil.jar getChildren 883bf7d 15\n");
		buf.append("  Example: java -jar cellutil.jar getLevel 883bf7d04\n");
		return buf.toString();
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println(CellUtils.getUsage());
			System.exit(0);
		} 

		String function = args[0];
		switch (function) {
			case "getCentroid":
				System.out.println(CellUtils.getCentroid(args[1]));
				break;
		
			case "getChildren":
				System.out.println(CellUtils.getChildren(args[1], args[2]));
				break;
				
			case "getLevel":
				System.out.println(CellUtils.getLevel(args[1]));
				break;
				
			default:
				System.out.println(CellUtils.getUsage());
				break;
		}
		
		

	}

}
