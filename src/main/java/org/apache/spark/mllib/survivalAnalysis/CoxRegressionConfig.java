package org.apache.spark.mllib.survivalAnalysis;

public class CoxRegressionConfig {
	private Integer id;
	private Integer MaxIter;
	private Double ConvergeThreshold;
	private String Method;
	private String RegularizationType;
	private Double RegularizationPara;
	private Integer Dim;

	public CoxRegressionConfig(Integer id, Integer maxIter, Double convergeThreshold,
			String method, String regularizationType,Double regularizationPara) {
		super();
		this.setId(id);
		MaxIter = maxIter;
		ConvergeThreshold = convergeThreshold;
		Method = method;
		RegularizationType = regularizationType;
		setRegularizationPara(regularizationPara);
		
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getMaxIter() {
		return MaxIter;
	}
	public void setMaxIter(Integer maxIter) {
		MaxIter = maxIter;
	}
	public Double getConvergeThreshold() {
		return ConvergeThreshold;
	}
	public void setConvergeThreshold(Double convergeThreshold) {
		ConvergeThreshold = convergeThreshold;
	}
	public String getMethod() {
		return Method;
	}
	public void setMethod(String method) {
		Method = method;
	}
	public String getRegularizationType() {
		return RegularizationType;
	}
	public void setRegularizationType(String regularizationType) {
		RegularizationType = regularizationType;
	}
	public Integer getDim() {
		return Dim;
	}
	public void setDim(Integer dim) {
		Dim = dim;
	}
	public Double getRegularizationPara() {
		return RegularizationPara;
	}
	public void setRegularizationPara(Double regularizationPara) {
		RegularizationPara = regularizationPara;
	}
	@Override
	public String toString() {
		return "CoxRegressionConfig [id=" + id + ", MaxIter=" + MaxIter
				+ ", ConvergeThreshold=" + ConvergeThreshold + ", Method="
				+ Method + ", RegularizationType=" + RegularizationType
				+ ", RegularizationPara=" + RegularizationPara + ", Dim=" + Dim
				+ "]";
	}
	

}
