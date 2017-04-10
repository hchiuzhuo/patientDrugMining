package org.imeds.feature.selection;

import java.util.ArrayList;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.imeds.util.LabelType;

public class discrimItemsets implements Comparable<discrimItemsets>{ 
	public static final int TYPE_INFO_GAIN					= 0001;
	public static final int TYPE_FISHER_GAIN				= 0002;
	private basicItemsets<Integer> itemsets = new basicItemsets<Integer>(); 
	private Integer support;
	private HashMap<Integer, ArrayList<Long>> inlabelCount = new  HashMap<Integer, ArrayList<Long>>();
	private HashMap<Integer, ArrayList<Long>> outlabelCount = new  HashMap<Integer, ArrayList<Long>>();
	private ArrayList<label> datapoints = new ArrayList<label>();
	private dataPointsStat dpstat = new dataPointsStat();
	private Double gain;
	private BitSet dataPointsBitset; 

	public discrimItemsets() {
		super();
	}

	public void addLabelCount(Integer label, Long itemsetId, Boolean in){
		HashMap<Integer, ArrayList<Long>> labelCount;
		if(in) labelCount = this.inlabelCount;
		else   labelCount = this.outlabelCount;
		
		if(!labelCount.containsKey(label)) labelCount.put(label, new ArrayList<Long>());
		labelCount.get(label).add(itemsetId);
	}
	public ArrayList<label> getDatapoints() {
		return datapoints;
	}

	public void setDatapoints(ArrayList<label> datapoints) {
		this.datapoints = datapoints;
	}
	public void addDatapoints(label data){
		this.datapoints.add(data);
	}
	public Double getGain(Integer gainType){
		
		switch(gainType){
		case TYPE_INFO_GAIN:
			
			break;
		case TYPE_FISHER_GAIN:
			this.gain = genFisherScore();
			break;
		}
		return this.gain;
	}

	
	public Double getGain() {
		return this.gain;
	}
	public void setGain(Double gain) {
		this.gain = gain;
	}
	public BitSet getDataPointsBitset() {
		return dataPointsBitset;
	}

	public void setDataPointsBitset(BitSet dataPointsBitset) {
		this.dataPointsBitset = dataPointsBitset;
	}
	
	public void setDataPointsBitset(int idx){
		this.dataPointsBitset.set(idx);		
	}


	public Double genFisherScore(){
		HashMap<Integer, statInfo> class_stat = new HashMap<Integer, statInfo>();
		statInfo sinfo;
	//	System.out.print(this.itemsets);
		for(label lb: this.datapoints){
			if(!class_stat.containsKey(lb.getLabel_id())) class_stat.put(lb.getLabel_id(), new statInfo());
			sinfo = class_stat.get(lb.getLabel_id());
			sinfo.addCnt();
			sinfo.addSum(lb.getFeature_v());
			sinfo.addSumSquare(lb.getFeature_v());
			//System.out.print(lb.getLabel_id()+" f:"+lb.getFeature_v()+" ");
		}
		//System.out.println();
		
		Iterator<Entry<Integer,statInfo>> itr = class_stat.entrySet().iterator();
		Double sum=0.0, cnt=0.0, mu=0.0, nom=0.0, denom=0.0;
		
		while (itr.hasNext()) {
			Entry<Integer, statInfo> entry = itr.next();
			cnt = cnt + entry.getValue().getCnt();
			sum  = sum  + entry.getValue().getSum();
		}
		mu = sum/cnt;
		itr = class_stat.entrySet().iterator();
		//System.out.print(class_stat.size()+"/"+sum+"/"+cnt+"="+mu);
		while (itr.hasNext()) {
			Entry<Integer, statInfo> entry = itr.next();
			nom	  = nom+(entry.getValue().getCnt()*Math.pow((entry.getValue().getMean()-mu), 2));
			denom = denom+(entry.getValue().getCnt()*entry.getValue().getVar());
		//	System.out.println(" nom:"+entry.getValue().getMean());
		}
		return (nom/denom);
	}
	public int compareTo(discrimItemsets bf) {
		 
		Double compareGain = (Double)bf.getGain();
		//descending order
		if( (compareGain -this.gain)>0 )
			return 1;
		else
			return 0;
	}

	public basicItemsets<Integer> getItemsets() {
		return itemsets;
	}
	public void setItemsets(basicItemsets<Integer> itemsets) {
		this.itemsets = itemsets;
	}

	public Integer getSupport() {
		return support;
	}

	public void setSupport(Integer support) {
		this.support = support;
	}

	public dataPointsStat getDpstat() {
		return dpstat;
	}

	public void setDpstat(dataPointsStat dpstat) {
		this.dpstat = dpstat;
	}
    
	public void calDpstat(){
		int featureN=0;
		for(label lb: this.getDatapoints()){
			if(lb.getFeature_v()==LabelType.yesFeature){
				featureN++;
				if((lb.getLabel_id()==LabelType.notOutlier) && (lb.getClass_id()==LabelType.alive)){
					this.dpstat.addLabel0_class1();
				}else if((lb.getLabel_id()==LabelType.notOutlier) && (lb.getClass_id()==LabelType.death)){
					this.dpstat.addLabel0_class0();
				}else if((lb.getLabel_id()==LabelType.yesOutlier) && (lb.getClass_id()==LabelType.alive)) {
					this.dpstat.addLabel1_class1();
				}else if((lb.getLabel_id()==LabelType.yesOutlier) && (lb.getClass_id()==LabelType.death)){
					this.dpstat.addLabel1_class0();
				}else{
					
				}	
			}
		}
	}
	@Override
	public String toString() {
//		return "discrimItemsets [itemsets=" + itemsets.toString() + ", datapoints="
//				+ datapoints.toString() + ", gain=" + gain + "]";
		return "discrimItemsets [itemsets=" + itemsets.toString()  + ", gain=" + gain + "]";
	}	


}
