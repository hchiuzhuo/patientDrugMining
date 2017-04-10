package org.imeds.feature.screening;

import java.util.ArrayList;
import java.util.BitSet;

import org.imeds.data.SurvivalTime;
import org.imeds.feature.selection.basicItemsets;

public class Tuple extends basicItemsets implements Comparable<Tuple>{
	private Long Id;

	private ArrayList<Double> yList = new ArrayList<Double>();
	private ArrayList<Double> xList = new ArrayList<Double>();
	private BitSet BitFeatures; 
	private Integer featureSize=0;
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}

	public ArrayList<Double> getxList() {
		return xList;
	}
	public void setxList(ArrayList<Double> xList) {
		this.xList = xList;
	}
	public void addxList(Double x){
		this.xList.add(x);
	}
	public ArrayList<Double> getyList() {
		return yList;
	}
	public void setyList(ArrayList<Double> yList) {
		this.yList = yList;
	}
	public  void addyList(Double y){
		this.yList.add(y);
	}

	public int compareTo(Tuple o) {
		// TODO Auto-generated method stub
		int result=0;
	    for(int i=0;i<this.yList.size();i++){
	    	result= Double.compare(this.getyList().get(i), o.getyList().get(i));
		    if (result != 0) {
		      return result;
		    }	
	    }
		return 0;	
	}
	public BitSet getBitFeatures() {
		return BitFeatures;
	}
	public void setBitFeatures(BitSet bitFeatures) {
		BitFeatures = bitFeatures;
	}
	public void setBitFeatures(int idx) {
		
		this.BitFeatures.set(idx);	
	}
	public Integer getFeatureSize() {
		return featureSize;
	}
	public void setFeatureSize(Integer featureSize) {
		this.featureSize = featureSize;
	}
}
