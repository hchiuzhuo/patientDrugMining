package org.apache.spark.mllib.survivalAnalysis;

import java.io.Serializable;

import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;

public class SurvivalLabelPoint extends LabeledPoint  implements Serializable {

	private double failed;
//	private Integer Id;
	private Long Id;
	public SurvivalLabelPoint(Long id,double failed, double label, Vector features) {
		super(label, features);
		this.setId(id);
		this.setFailed(failed);
	}
	public double getFailed() {
		return failed;
	}
	public void setFailed(double failed) {
		this.failed = failed;
	}
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	@Override
	public String toString() {
		return "SurvivalLabelPoint [Id="+this.Id+" failed=" + failed + ", label()=" + label()
				+ ", features()=" + features() ;
	}

	
}
