package org.imeds.util;

import java.util.ArrayList;

public class ImedStringFormat {

	public ImedStringFormat() {
		// TODO Auto-generated constructor stub
	}
	public static String tranListIn(ArrayList<Integer> lst){
		StringBuffer str = new StringBuffer();
		
		for(Integer id: lst){
			str.append(id+",");
		}
		str.delete(str.lastIndexOf(","), str.length());
		return str.toString();
	}
	
	public static String toLinuxPath(String path){
		path = path.replaceAll("\\", "//");
		return path;
	}
}
