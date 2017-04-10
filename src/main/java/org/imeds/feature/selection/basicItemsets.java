package org.imeds.feature.selection;

import java.util.ArrayList;
import java.util.TreeSet;

public class basicItemsets<T> {
	private Long id;
	private ArrayList<TreeSet<T>> itemsets = new  ArrayList<TreeSet<T>>(); 
	private Object rcMutex = new Object();
	private int referenceCount = 0;
	
	public void increaseReferenceCount() {
    	synchronized(this.rcMutex) {
    		this.referenceCount++;
    	}
    }

    public void decreaseReferenceCount() {
    	synchronized(this.rcMutex) {
    		this.referenceCount--;
    	}
    }
    public void resetReferenceCount() {
    	synchronized(this.rcMutex) {
    		this.referenceCount=0;
    	}
    }


	public int getReferenceCount() {
        return this.referenceCount;
    }
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ArrayList<TreeSet<T>> getItemsets() {
		return itemsets;
	}

	public void setItemsets(ArrayList<TreeSet<T>> itemsets) {
		this.itemsets = itemsets;
	}

	public void setItemset(TreeSet<T> itemset){
		this.itemsets.add(itemset);
	}
	
	public TreeSet<T> getItemset(int i){
		return this.itemsets.get(i);
	}
	public void setItem(int i, T item){
		this.itemsets.get(i).add(item);
		
	}
	
	public boolean hasItemset(int i, TreeSet<T> itemset){
		if(this.itemsets.get(i).containsAll(itemset))return true;
		else return false;
	}
	public basicItemsets() {
		
	}
	public basicItemsets( Long id) {
		this.id = id;
	}

	public boolean isContained(basicItemsets<T> its){
		boolean rs = false;
		int its_idx = 0;
		int my_idx = 0;
		if(its.getItemsets().size()<=this.getItemsets().size()){
			while(its_idx<its.getItemsets().size()){
				TreeSet<T> its_treeSet = its.getItemset(its_idx);
				while(my_idx<this.getItemsets().size()){
					if(this.hasItemset(my_idx, its_treeSet)){
						rs = true;
						my_idx++;
						break;
					}else{
						rs = false;
						my_idx++;
					}
				}
				
				its_idx++;
				if(my_idx>=this.getItemsets().size()){
					if(its_idx < its.getItemsets().size())rs = false;
					break;
				}
				
			}		
		}
		return rs;
	}

	@Override
	public String toString() {
		return "basicItemsets [id=" + id + ", itemsets=" + itemsets.toString()
				+ ",  referenceCount=" + referenceCount
				+ "]";
	}

}
