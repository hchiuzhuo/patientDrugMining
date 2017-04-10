package org.imeds.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

public class ComorbidDataSetConfig extends DataSetConfig {
	private ArrayList<String> index_diagnoses = new ArrayList<String>();
	private boolean LRsampleEnable=false;
	private HashMap<String, sampleConfig> sample_sets = new HashMap<String, sampleConfig>(); 
	
	
	private String pearsonResidualOutlierInputFolder;
	private String pearsonResidualOutlierOutputFolder;
	private Double pearsonResidualThreshold;
	private Double chiSqrtThreshold;
	private ArrayList<String> sparkLRmodelParas = new ArrayList<String>();
	private ArrayList<String> sparkLRmodelDataSets = new ArrayList<String>();

	

	
	private ArrayList<String> fittedSparkLRmodelParas = new ArrayList<String>();
	private String preseqOutputFolder;
	private Double outlierThreshold;
	
	public ArrayList<String> getIndex_diagnoses() {
		return index_diagnoses;
	}

	public void setIndex_diagnoses(ArrayList<String> index_diagnoses) {
		this.index_diagnoses = index_diagnoses;
	}
	public void setIndex_diagnoses(List<Element> index_diagnoses) {
		for(Element ele:index_diagnoses){
			this.index_diagnoses.add(ele.getText());
		}
	//	this.index_diagnoses = index_diagnoses;
	}
	public void setColList(List<Element> colList) {
		for(Element ele: colList){
			this.colList.add(ele.getText());
			for(String idx_dia:this.index_diagnoses){
				if(idx_dia.equals(ele.getText())){
					this.colList.remove(idx_dia);
					break;
				}
			}			
		}
	}

	public HashMap<String, sampleConfig> getSample_sets() {
		return sample_sets;
	}
	

	public void setSample_sets(List<Element> sampleConfigList) {
	
		for(Element ele:sampleConfigList){
			String sample_id=ele.element("sample_id").getText();
			String sample_label=ele.elementText("sample_label");
			String sample_random=ele.elementText("sample_random");
			String sample_range_str = ele.elementText("sample_range");
			String sample_append = ele.elementText("sample_append");
			sampleConfig scfg = new sampleConfig(sample_id, sample_label, sample_random, sample_range_str, sample_append);
			setSample_sets(sample_id, scfg);
		}
	}
	public void setSample_sets(String sample_id, sampleConfig sample_config) {
		this.sample_sets.put(sample_id, sample_config);
	}
	public boolean isLRsampleEnable() {
		return LRsampleEnable;
	}

	public void setLRsampleEnable(boolean lRsampleEnable) {
		LRsampleEnable = lRsampleEnable;
	}

	
	public String getPearsonResidualOutlierInputFolder() {
		return pearsonResidualOutlierInputFolder;
	}

	public void setPearsonResidualOutlierInputFolder(
			String pearsonResidualOutlierInputFolder) {
		this.pearsonResidualOutlierInputFolder = pearsonResidualOutlierInputFolder;
	}

	public String getPearsonResidualOutlierOutputFolder() {
		return pearsonResidualOutlierOutputFolder;
	}

	public void setPearsonResidualOutlierOutputFolder(
			String pearsonResidualOutlierOutputFolder) {
		this.pearsonResidualOutlierOutputFolder = pearsonResidualOutlierOutputFolder;
	}

	public Double getPearsonResidualThreshold() {
		return pearsonResidualThreshold;
	}

	public void setPearsonResidualThreshold(Double pearsonResidualThreshold) {
		this.pearsonResidualThreshold = pearsonResidualThreshold;
	}

	public Double getChiSqrtThreshold() {
		return chiSqrtThreshold;
	}

	public void setChiSqrtThreshold(Double chiSqrtThreshold) {
		this.chiSqrtThreshold = chiSqrtThreshold;
	}

	public ArrayList<String> getSparkLRmodelParas() {
		return sparkLRmodelParas;
	}

	public void setSparkLRmodelParas(ArrayList<String> sparkLRmodelParas) {
		this.sparkLRmodelParas = sparkLRmodelParas;
	}
	public void setSparkLRmodelParas(List<Element> colList) {
		for(Element ele: colList){
			this.sparkLRmodelParas.add(ele.getText());			
		}
	}
	public ArrayList<String> getSparkLRmodelDataSets() {
		return sparkLRmodelDataSets;
	}

	public void setSparkLRmodelDataSets(ArrayList<String> sparkLRmodelDataSets) {
		this.sparkLRmodelDataSets = sparkLRmodelDataSets;
	}
	public void setSparkLRmodelDataSets(List<Element> colList) {
		for(Element ele: colList){
			this.sparkLRmodelDataSets.add(ele.getText());			
		}
	}
	
	public ArrayList<String> getFittedSparkLRmodelParas() {
		return fittedSparkLRmodelParas;
	}

	public void setFittedSparkLRmodelParas(ArrayList<String> fittedSparkLRmodelParas) {
		this.fittedSparkLRmodelParas = fittedSparkLRmodelParas;
	}
	public void setFittedSparkLRmodelParas(List<Element> colList) {
		for(Element ele: colList){
			this.fittedSparkLRmodelParas.add(ele.getText());			
		}
	}

	public String getPreseqOutputFolder() {
		return preseqOutputFolder;
	}

	public void setPreseqOutputFolder(String preseqOutputFolder) {
		this.preseqOutputFolder = preseqOutputFolder;
	}

	public Double getOutlierThreshold() {
		return outlierThreshold;
	}

	public void setOutlierThreshold(Double outlierThreshold) {
		this.outlierThreshold = outlierThreshold;
	}

	public ComorbidDataSetConfig() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return super.toString()+ "\n ComorbidDataSetConfig [index_diagnoses=" + index_diagnoses
				+ "]";
	}

}
