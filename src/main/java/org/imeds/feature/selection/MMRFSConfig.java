package org.imeds.feature.selection;

public class MMRFSConfig {
	private String basicItemsetsFileName;
	private String discrimItemsetsFileName;
	private String labelBase;
	private Double labelDefineThreshold;
	private Double coverageRate;
	private String outlierSource;
//	private Double outlierThreshold;
	private String featureItemsetFileName;
	public MMRFSConfig() {
		// TODO Auto-generated constructor stub
	}

	public String getBasicItemsetsFileName() {
		return basicItemsetsFileName;
	}

	public void setBasicItemsetsFileName(String basicItemsetsFileName) {
		this.basicItemsetsFileName = basicItemsetsFileName;
	}

	public String getDiscrimItemsetsFileName() {
		return discrimItemsetsFileName;
	}

	public void setDiscrimItemsetsFileName(String discrimItemsetsFileName) {
		this.discrimItemsetsFileName = discrimItemsetsFileName;
	}

	public String getLabelBase() {
		return labelBase;
	}

	public void setLabelBase(String labelBase) {
		this.labelBase = labelBase;
	}

	public Double getLabelDefineThreshold() {
		return labelDefineThreshold;
	}

	public void setLabelDefineThreshold(Double labelDefineThreshold) {
		this.labelDefineThreshold = labelDefineThreshold;
	}

	public Double getCoverageRate() {
		return coverageRate;
	}

	public void setCoverageRate(Double coverageRate) {
		this.coverageRate = coverageRate;
	}


	
	public String getOutlierSource() {
		return outlierSource;
	}

	public void setOutlierSource(String outlierSource) {
		this.outlierSource = outlierSource;
	}

//	public Double getOutlierThreshold() {
//		return outlierThreshold;
//	}
//
//	public void setOutlierThreshold(Double outlierThreshold) {
//		this.outlierThreshold = outlierThreshold;
//	}

	public String getFeatureItemsetFileName() {
		return featureItemsetFileName;
	}

	public void setFeatureItemsetFileName(String featureItemsetFileName) {
		this.featureItemsetFileName = featureItemsetFileName;
	}

	@Override
	public String toString() {
		return "MMRFSConfig [basicItemsetsFileName=" + basicItemsetsFileName
				+ "\n, discrimItemsetsFileName=" + discrimItemsetsFileName
				+ "\n, labelDefineThreshold=" + labelDefineThreshold
				+ "\n, coverageRate=" + coverageRate + ", outlierSource="
				+ outlierSource + "\n, featureItemsetFileName="
				+ featureItemsetFileName + "]";
	}

}
