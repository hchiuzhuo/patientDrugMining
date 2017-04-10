package org.imeds.feature.selection;

public class labelItemsets  {
	private Integer label;
	private basicItemsets<Integer> itemsets = new basicItemsets<Integer>(); 
	public Integer getLabel() {
		return label;
	}
	public void setLabel(Integer label) {
		this.label = label;
	}
	public basicItemsets<Integer> getItemsets() {
		return itemsets;
	}
	public void setItemsets(basicItemsets<Integer> itemsets) {
		this.itemsets = itemsets;
	}

	
}
