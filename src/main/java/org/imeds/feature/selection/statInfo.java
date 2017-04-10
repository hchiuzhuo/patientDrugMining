package org.imeds.feature.selection;

public class statInfo {
	public Double cnt=0.0;
	public Double sum=0.0;
	public Double sumSquare = 0.0;
	
	public Double getCnt() {
		return cnt;
	}
	public void setCnt(Double cnt) {
		this.cnt = cnt;
	}
	public void addCnt(){
		this.cnt++;
	}
	
	public Double getSum() {
		return sum;
	}
	public void setSum(Double sum) {
		this.sum = sum;
	}
	public void addSum(Double p){
		this.sum = this.sum + p;
	}
	
	public Double getSumSquare() {
		return sumSquare;
	}
	public void setSumSquare(Double sumSquare) {
		this.sumSquare = sumSquare;
	}

	public void addSumSquare(Double p){
		this.sumSquare = this.sumSquare + Math.pow(p, 2.0);
	}
	public Double getMean(){
		return (this.sum/this.cnt);
	}
	public Double getVar(){
		return (this.sumSquare/this.cnt)-Math.pow((this.sum/this.cnt),2.0);
	}
	public Double getStd(){
		return Math.pow(getVar(), 0.5);
	}
	public statInfo() {
		// TODO Auto-generated constructor stub
	}

}
