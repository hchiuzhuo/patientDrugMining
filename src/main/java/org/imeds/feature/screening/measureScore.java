package org.imeds.feature.screening;

public class measureScore {
	private Integer id;
	private Double score;
	private String comment;
	private long[] trainTime;
	private long[] testTime;
	public measureScore() {
	}
	public measureScore(Integer id, Double score, String comment) {
		super();
		this.id = id;
		this.score = score;
		this.comment = comment;
	}
	public measureScore(Integer id, Double score, String comment, long[] trainTime, long[]testTime) {
		super();
		this.id = id;
		this.score = score;
		this.comment = comment;
		this.setTrainTime(trainTime);
		this.setTestTime(testTime);
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public long[] getTrainTime() {
		return trainTime;
	}
	public String getTrainTimeStr() {		
		return trainTime[1]+":"+trainTime[2]+":"+ trainTime[3]+"."+trainTime[4];
	}
	public void setTrainTime(long[] trainTime) {
		this.trainTime = trainTime;
	}
	public long[] getTestTime() {
		return testTime;
	}
	public String getTestTimeStr() {		   
		return testTime[1]+":"+testTime[2]+":"+ testTime[3]+"."+testTime[4];	     
	}
	public void setTestTime(long[] testTime) {
		this.testTime = testTime;
	}
}
