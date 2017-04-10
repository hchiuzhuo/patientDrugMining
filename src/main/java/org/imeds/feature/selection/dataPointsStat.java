package org.imeds.feature.selection;

public class dataPointsStat {
	private Double label0_class0 = 0.0; //label as not outlier, actual die
	private Double label1_class1 = 0.0; //label as outlier (regard he'll die). actual alive.
	private Double label0_class1 = 0.0; //label as not outlier, actual alive
	private Double label1_class0 = 0.0; //label as outlier (regard he'll alive). actual die

	public Double not_outlier_survival_rate(){
		return (this.label0_class1/(this.label0_class0+this.label0_class1));
	}
	public Double outlier_survival_rate(){
		return (this.label1_class1/(this.label1_class0+this.label1_class1));
	}
	public Double seq_survival_rate(){
		return ((this.label0_class1+this.label1_class1)/(this.label0_class0+this.label0_class1+this.label1_class0+this.label1_class1));
	}
	//label as not outlier, actual die
	public Double getLabel0_class0() {
		return label0_class0;
	}
	public void setLabel0_class0(Double label0_class0) {
		this.label0_class0 = label0_class0;
	}
	public void addLabel0_class0() {
		this.label0_class0++;
	}
	
	//label as outlier (regard he'll die). actual alive
	public Double getLabel1_class1() {
		return label1_class1;
	}
	public void setLabel1_class1(Double label1_class1) {
		this.label1_class1 = label1_class1;
	}
	public void addLabel1_class1() {
		this.label1_class1++;
	}
	
	//label as not outlier, actual alive
	public Double getLabel0_class1() {
		return label0_class1;
	}	
	public void setLabel0_class1(Double label0_class1) {
		this.label0_class1 = label0_class1;
	}
	public void addLabel0_class1() {
		this.label0_class1++;
	}
	
	//label as outlier (regard he'll alive). actual die
	public Double getLabel1_class0() {
		return label1_class0;
	}
	public void setLabel1_class0(Double label1_class0) {
		this.label1_class0 = label1_class0;
	}
	public void addLabel1_class0() {
		this.label1_class0++;
	}
}
