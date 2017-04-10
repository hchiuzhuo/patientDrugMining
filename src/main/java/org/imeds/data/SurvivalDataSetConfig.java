package org.imeds.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.imeds.daemon.SeqptnManager;
import org.imeds.util.ImedDateFormat;
import org.imeds.util.OSValidator;

public class SurvivalDataSetConfig extends ComorbidDataSetConfig {
	private boolean SurvivalsampleEnable=false;
	private sampleConfig SurvivalDataSet = new sampleConfig();
	private String svltargetFileName="";
	private ArrayList<Date> censorDate=new ArrayList<Date>();
	private ArrayList<String> svlcolList=new ArrayList<String>();
	
	private String coxResidualOutlierInputFolder;
	private String coxResidualOutlierOutputFolder;

	public String getSvltargetFileName() {
		return svltargetFileName;
	}

	public void setSvltargetFileName(String svltargetFileName) {
		this.svltargetFileName = svltargetFileName;
		if(!OSValidator.isWindows()){
			this.svltargetFileName =this.svltargetFileName.replace("\\", "/");			
		}
	}
	
	public ArrayList<Date> getCensorDate() {
		return censorDate;
	}

	public void setCensorDate(ArrayList<Date> censorDate) {
		this.censorDate = censorDate;
	}
	public void setCensorDate(List<Element> censorDate) throws Exception {
		for(Element ele: censorDate){
			this.censorDate.add(ImedDateFormat.parse(ele.getText()));					
		}
	}
	
	public ArrayList<String> getSvlcolList() {
		return svlcolList;
	}
	public void setSvlcolList(List<Element> colList) {
		for(Element ele: colList){
			this.svlcolList.add(ele.getText());					
		}
	}
	public void setSvlcolList(ArrayList<String> svlcolList) {
		this.svlcolList = svlcolList;
	}

	public boolean isSurvivalsampleEnable() {
		return SurvivalsampleEnable;
	}

	public void setSurvivalsampleEnable(boolean survivalsampleEnable) {
		SurvivalsampleEnable = survivalsampleEnable;
	}

	public sampleConfig getSurvivalDataSet() {
		return SurvivalDataSet;
	}
	public void setSurvivalDataSet(Element ele) {
		String sample_random=ele.elementText("sample_random");
		String sample_range_str = ele.elementText("sample_range");
		String sample_append = ele.elementText("sample_append");
		sampleConfig scfg = new sampleConfig(sample_random, sample_range_str, sample_append);
		setSurvivalDataSet(scfg);
		
	}
	public void setSurvivalDataSet(sampleConfig survivalDataSet) {
		SurvivalDataSet = survivalDataSet;
	}
	
	public String getCoxResidualOutlierInputFolder() {
		return coxResidualOutlierInputFolder;
	}

	public void setCoxResidualOutlierInputFolder(
			String coxResidualOutlierInputFolder) {
		this.coxResidualOutlierInputFolder = coxResidualOutlierInputFolder;
	}

	public String getCoxResidualOutlierOutputFolder() {
		return coxResidualOutlierOutputFolder;
	}

	public void setCoxResidualOutlierOutputFolder(
			String coxResidualOutlierOutputFolder) {
		this.coxResidualOutlierOutputFolder = coxResidualOutlierOutputFolder;
	}


}
