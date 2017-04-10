package org.apache.spark.mllib.survivalAnalysis;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

public class CoxProcessConfig {
	private String DatasetPath;
	private String SchemaPath;
	private String X_varListPath;

	private String X_varList;
	private Integer Dim;
	private String Censored_var;
	private String Y_var;
	
	private Boolean enableKfold=false;
	private Integer Kfold=1;	
	private Boolean enablePvalue=false;

	
	private String ModelOutputPath;
	private String StatsSummaryPath;
	private Boolean enableCstats=false;

	private String PredictionResultPath;
	private ArrayList<CoxRegressionConfig> AlgCfgList = new ArrayList<CoxRegressionConfig>(); 
	public String getDatasetPath() {
		return DatasetPath;
	}
	public void setDatasetPath(String datasetPath) {
		DatasetPath = datasetPath;
	}
	
	public String getSchemaPath() {
		return SchemaPath;
	}
	public void setSchemaPath(String schemaPath) {
		SchemaPath = schemaPath;
	}
	public String getX_varListPath() {
		return X_varListPath;
	}
	public void setX_varListPath(String x_varListPath) {
		
		X_varListPath = x_varListPath.trim().replace("\"", "");
		
	}
	public String getX_varList() {
		return X_varList;
	}
	public void setX_varList(String x_varList) {
		X_varList = x_varList.trim().replace("\"", "");
		String[] X_varStr = X_varList.split(",");
		this.setDim(X_varStr.length);
	}
	public Integer getDim() {
		return Dim;
	}
	public void setDim(Integer dim) {
		Dim = dim;
	}
	public String getCensored_var() {
		return Censored_var;
	}
	public void setCensored_var(String censored_var) {
		Censored_var = censored_var.trim().replace("\"", "");;
	}
	public String getY_var() {
		return Y_var;
	}
	public void setY_var(String y_var) {
		Y_var = y_var.trim().replace("\"", "");;
	}
	
	public Boolean getEnableKfold() {
		return enableKfold;
	}
	public void setEnableKfold(Boolean enableKfold) {
		this.enableKfold = enableKfold;
	}
	public Integer getKfold() {
		return Kfold;
	}
	public void setKfold(Integer kfold) {
		Kfold = kfold;
	}
	public Boolean getEnablePvalue() {
		return enablePvalue;
	}
	public void setEnablePvalue(Boolean enablePvalue) {
		this.enablePvalue = enablePvalue;
	}

	public String getModelOutputPath() {
		return ModelOutputPath;
	}
	public void setModelOutputPath(String modelOutputPath) {
		ModelOutputPath = modelOutputPath;
	}
	public String getStatsSummaryPath() {
		return StatsSummaryPath;
	}
	public void setStatsSummaryPath(String statsSummaryPath) {
		StatsSummaryPath = statsSummaryPath;
	}
	public Boolean getEnableCstats() {
		return enableCstats;
	}
	public void setEnableCstats(Boolean enableCstats) {
		this.enableCstats = enableCstats;
	}
	public String getPredictionResultPath() {
		return PredictionResultPath;
	}
	public void setPredictionResultPath(String predictionResultPath) {
		PredictionResultPath = predictionResultPath;
	}
	public ArrayList<CoxRegressionConfig> getAlgCfgList() {
		return AlgCfgList;
	}
	public void setAlgCfgList(ArrayList<CoxRegressionConfig> algCfgList) {
		AlgCfgList = algCfgList;
	}
	public void addAlgCfgList(CoxRegressionConfig algCfg) {
		this.AlgCfgList.add(algCfg);
	}
	public void setAlgCfgList(List<Element> sampleConfigList) {
		
		for(Element ele:sampleConfigList){
			Integer sample_id=0;
			Integer MaxIter=10;
			Double ConvergeThreshold=0.001;
			String Method="CLG";
			String RegularizationType="NONE";
			Double RegularizationPara=1.0;
			if(ele.element("id").getText()!=null && !ele.element("id").getText().trim().equals("")) 
				sample_id=Integer.parseInt(ele.element("id").getText());
			
			if(ele.element("MaxIter").getText()!=null && !ele.element("MaxIter").getText().trim().equals(""))
				MaxIter = Integer.parseInt(ele.elementText("MaxIter").trim());
			
			if(ele.element("ConvergeThreshold").getText()!=null && !ele.element("ConvergeThreshold").getText().trim().equals(""))
				ConvergeThreshold = Double.parseDouble(ele.elementText("ConvergeThreshold").trim());
			
			if(ele.element("Method").getText()!=null && !ele.element("Method").getText().trim().equals(""))
				Method=ele.element("Method").getText().trim();
			
			if(ele.element("RegularizationType").getText()!=null && !ele.element("RegularizationType").getText().trim().equals(""))
				RegularizationType=ele.element("RegularizationType").getText().trim();

			if(ele.element("RegularizationPara").getText()!=null && !ele.element("RegularizationPara").getText().trim().equals(""))
				RegularizationPara = Double.parseDouble(ele.elementText("RegularizationPara").trim());
			
			 
			this.addAlgCfgList(new CoxRegressionConfig(sample_id,MaxIter,ConvergeThreshold,Method,RegularizationType,RegularizationPara));
			
		}
	}
	@Override
	public String toString() {
		return "CoxProcessConfig [DatasetPath=" + DatasetPath + ", SchemaPath="
				+ SchemaPath + ", X_varListPath=" + X_varListPath
				+ ", X_varList=" + X_varList + ", Dim=" + Dim
				+ ", Censored_var=" + Censored_var + ", Y_var=" + Y_var
				+ ", enableKfold=" + enableKfold + ", Kfold=" + Kfold
				+ ", enablePvalue=" + enablePvalue + ", ModelOutputPath="
				+ ModelOutputPath + ", StatsSummaryPath=" + StatsSummaryPath
				+ ", PredictionResultPath=" + PredictionResultPath
				+ ", AlgCfgList=" + AlgCfgList + "]";
	}
	
	
}
