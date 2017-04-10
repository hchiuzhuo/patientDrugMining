package org.imeds.data;

import java.util.ArrayList;

import org.imeds.util.OSValidator;

public class DataSetConfig {
	
	private String targetFileName="";
	protected ArrayList<String> colList=new ArrayList<String>();
	

	public String getTargetFileName() {
		return targetFileName;
	}

	public void setTargetFileName(String targetFileName) {
	
		this.targetFileName = targetFileName;
		if(!OSValidator.isWindows()){
			this.targetFileName =this.targetFileName.replace("\\", "/");			
		}
	}

	public ArrayList<String> getColList() {
		return colList;
	}

	public void setColList(ArrayList<String> colList) {
		this.colList = colList;
	}

	public DataSetConfig() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "DataSetConfig [ targetFileName=" + targetFileName + ", colList=" + colList
				+ "]";
	}

}
