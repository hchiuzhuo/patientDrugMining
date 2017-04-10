package org.apache.spark.mllib.survivalAnalysis;

import java.util.ArrayList;
import java.util.Arrays;

import JSci.maths.statistics.NormalDistribution;

public class CoxModel {
	public ArrayList<String>header=new ArrayList<String>();
	private String X_varList;
	private ArrayList<String> var =new ArrayList<String>();
	private ArrayList<Double> coef=new ArrayList<Double>();
	private ArrayList<Double> testStats=new ArrayList<Double>();
	private ArrayList<Double> se=new ArrayList<Double>();
	private ArrayList<Double> pValue=new ArrayList<Double>();
	private NormalDistribution normalDistribution= new NormalDistribution();

	private Double censoredN=0.0;
	private Double failedN=0.0;
	private Double totalN=0.0;
	private String dataFilePath;
	private String modelFilePath;
	private String configFilePath;
	private Integer configIdx;
	private long[] trainingTime;
	private Double c_statistic=0.0;
	
	private String configDesp="";
	public CoxModel(){
		this.header.add("var");
		this.header.add("coef");
		this.header.add("exp(coef)");
		this.header.add("se(coef)");
		this.header.add("z");
		this.header.add("Pr(>|z|)");
	}
	public String getX_varList() {
		return X_varList;
	}
	public void setX_varList(String x_varList) {
		X_varList = x_varList;
		String[] x_var = X_varList.split(",");
		for(int i=0;i<x_var.length;i++)var.add(x_var[i]);
	}
	public ArrayList<String> getVar() {
		return var;
	}
	public void setVar(ArrayList<String> var) {
		this.var = var;
	}
	public ArrayList<Double> getCoef() {
		return coef;
	}
	public void setCoef(ArrayList<Double> coef) {
		this.coef = coef;
	}
	public void setCoef(double[] coef) {
		for(int i=0;i<coef.length;i++)this.coef.add(coef[i]);
		
	}
	public ArrayList<Double> getTestStats() {
		return testStats;
	}
	public void setTestStats(ArrayList<Double> testStats) {
		this.testStats = testStats;
	}
	
	public void addTestStats(Double testStatValue, int idx){
		this.testStats.add(idx, testStatValue);
		Double p_value =2 * (1 - normalDistribution.cumulative(Math.abs(testStatValue)));
		this.pValue.add(idx, p_value);
		this.se.add(idx,Math.sqrt(testStatValue));
	}
	public ArrayList<Double> getpValue() {
		return pValue;
	}
	public void setpValue(ArrayList<Double> pValue) {
		this.pValue = pValue;
	}
	public Double getCensoredN() {
		return censoredN;
	}
	public void setCensoredN(Double censoredN) {
		this.censoredN = censoredN;
	}
	public Double getFailedN() {
		return failedN;
	}
	public void setFailedN(Double failedN) {
		this.failedN =failedN;
	}
	public Double getTotalN() {
		return totalN;
	}
	public void setTotalN(Double totalN) {
		this.totalN = totalN;
	}
	public String getDataFilePath() {
		return dataFilePath;
	}
	public void setDataFilePath(String dataFilePath) {
		this.dataFilePath = dataFilePath;
	}
	public String getModelFilePath() {
		return modelFilePath;
	}
	public void setModelFilePath(String modelFilePath) {
		this.modelFilePath = modelFilePath;
	}
	public String getConfigFilePath() {
		return configFilePath;
	}
	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}
	public Integer getConfigIdx() {
		return configIdx;
	}
	public void setConfigIdx(Integer configIdx) {
		this.configIdx = configIdx;
	}
	public long[] getTrainingTime() {
		return trainingTime;
	}
	public void setTrainingTime(long[] trainingTime) {
		this.trainingTime = trainingTime;
	}
	public ArrayList<Double> getSe() {
		return se;
	}
	public void setSe(ArrayList<Double> se) {
		this.se = se;
	}
	public Double getC_statistic() {
		return c_statistic;
	}
	public void setC_statistic(Double c_statistic) {
		this.c_statistic = c_statistic;
	}
	@Override
	public String toString() {
		return "CoxModel [X_varList=" + X_varList + ", var=" + var + ", coef="
				+ coef + ", testStats=" + testStats + ", pValue=" + pValue
				+ "]";
	}
	public String getConfigDesp() {
		return configDesp;
	}
	public void setConfigDesp(String configDesp) {
		this.configDesp = configDesp;
	}
	

}
