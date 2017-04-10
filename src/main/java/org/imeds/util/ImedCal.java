package org.imeds.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ImedCal {

	public ImedCal() {
		// TODO Auto-generated constructor stub
	}   
	
	public static String double_format(double num, int digit){
		String dgf = "#.";
		for(int i=0;i<digit;i++){
			dgf=dgf+"#";
		}
		DecimalFormat df=new DecimalFormat(dgf);
		return df.format(num);
	}
	public static Double getI(HashMap<Integer,Double> map){
		Double i = 0.0;
		Double total=0.0;
		 Iterator<Entry<Integer,Double>> itemIter = map.entrySet().iterator();
	      while(itemIter.hasNext()){
	    	  Entry<Integer,Double> item = itemIter.next();	
	    	  total = total+ item.getValue();
	      }
	      
	      itemIter = map.entrySet().iterator();
	      while(itemIter.hasNext()){
	    	  Entry<Integer,Double> item = itemIter.next();
	    	  Double vtmp = 0.000001;
	    	  if(item.getValue()>0.0) vtmp = item.getValue(); 
	    	 i = i +(-(item.getValue()/total)*(Math.log(vtmp/total))/Math.log(2.0));
	      }
		return i;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
//		map.put(0, 6.0);
//		map.put(1, 0.0);
//		Double I = ImedCal.getI(map);
//		System.out.println("I(4,2)="+I);
		double     x=12.654124;
		DecimalFormat df=new DecimalFormat("#.##");
		String s=df.format(x);   
		System.out.println(s+","+ImedCal.double_format(0.02345677, 3));
		
	}

}
